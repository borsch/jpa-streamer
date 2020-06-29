package com.speedment.jpastreamer.fieldgenerator.standard.internal;

import com.speedment.common.codegen.Generator;
import com.speedment.common.codegen.constant.SimpleParameterizedType;
import com.speedment.common.codegen.constant.SimpleType;
import com.speedment.common.codegen.controller.AlignTabs;
import com.speedment.common.codegen.controller.AutoImports;
import com.speedment.common.codegen.internal.java.JavaGenerator;
import com.speedment.common.codegen.model.*;
import com.speedment.common.codegen.model.Class;
import com.speedment.common.codegen.model.Field;
import com.speedment.jpastreamer.field.*;
import com.speedment.jpastreamer.fieldgenerator.standard.exception.FieldGeneratorProcessorException;
import com.speedment.jpastreamer.typeparser.standard.StandardTypeParser;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.persistence.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.Enum;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.Clob;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.speedment.common.codegen.util.Formatting.*;
import static com.speedment.jpastreamer.fieldgenerator.standard.util.GeneratorUtil.*;

/**
 * JPAStreamer standard annotation processor that generates fields for classes annotated
 * with {@code Entity}.
 *
 * @author Julia Gustafsson
 * @since 0.0.9
 */

public final class InternalFieldGeneratorProcessor extends AbstractProcessor {

    protected static final String GETTER_METHOD_PREFIX = "get";

    private static final Generator generator = Generator.forJava();

    private ProcessingEnvironment processingEnvironment;
    private Elements elementUtils;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        this.processingEnvironment = env;
        this.elementUtils = processingEnvironment.getElementUtils();

        messager = processingEnvironment.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "JPA Streamer Field Generator Processor");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if(annotations.size() == 0 || roundEnv.processingOver()) {
            // Allow other processors to run
            return false;
        }

        roundEnv.getElementsAnnotatedWith(Entity.class).stream()
                .filter(ae -> ae.getKind() == ElementKind.CLASS)
                .forEach(ae -> {
                    try {
                        generateFields(ae);
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                });

        return true;
    }

    private void generateFields(Element annotatedElement) throws IOException {

        // Retrieve all declared fields of the annotated class
        Set<? extends Element> enclosedFields = annotatedElement.getEnclosedElements().stream()
                .filter(ee -> ee.getKind().isField()
                        && !ee.getModifiers().contains(Modifier.FINAL)) // Ignore immutable fields
                .collect(Collectors.toSet());

        String entityName = shortName(annotatedElement.asType().toString());
        String genEntityName = entityName + "$";
        String qualifiedGenEntityName = annotatedElement.asType().toString() + "$";

        PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(annotatedElement);
        String packageName;
        if (packageElement.isUnnamed()) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Class " + entityName + "has an unnamed package.");
            packageName = "";
        } else {
            packageName = packageElement.getQualifiedName().toString();
        }

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(qualifiedGenEntityName);
        Writer writer = builderFile.openWriter();

        File file = generatedEntity(enclosedFields, entityName, genEntityName, packageName);
        writer.write(new JavaGenerator().on(file).get());
        writer.close();
    }

    private File generatedEntity(Set<? extends Element> enclosedFields, String entityName, String genEntityName, String packageName) {
        File file = packageName.isEmpty() ?
                File.of(genEntityName + ".java") :
                File.of(packageName + "/" + genEntityName + ".java");
        Class clazz = Class.of(genEntityName).public_()
                .set(Javadoc.of(
                        "The generated base for entity {@link " + entityName + "} representing entities of the"
                                + " {@code " + lcfirst(entityName) + "}-table in the database." +
                                nl() + "<p> This file has been automatically generated by JPAStreamer."
                ).author("JPAStreamer"));

        enclosedFields
                .forEach(field -> {
                    addFieldToClass(field, clazz, entityName);
                });

        file.add(clazz);
        file.call(new AutoImports(generator.getDependencyMgr())).call(new AlignTabs<>());
        return file;
    }

    private void addFieldToClass(Element field, Class clazz, String entityName) {
        String fieldName = field.getSimpleName().toString();
        Column col = field.getAnnotation(Column.class);

        java.lang.Class fieldClass;
        try {
            messager.printMessage(Diagnostic.Kind.NOTE, "parsing type: " + fieldType(field).getTypeName());
            fieldClass = parseType(fieldType(field).getTypeName());
        } catch (IllegalArgumentException e) {
            throw new FieldGeneratorProcessorException("Type with name " + fieldType(field).getTypeName() + " was not found.");
        }

        Type referenceType = referenceType(field, fieldClass, entityName);

        // Begin building the field value parameters
        final List<Value<?>> fieldParams = new ArrayList<>();

        // Add table entity
        fieldParams.add(Value.ofReference(entityName + ".class"));

        // Add db column name if stated, else fall back on entity field name
        fieldParams.add(Value.ofText((col != null && !col.name().isEmpty()) ? col.name() : fieldName));

        // Add getter method reference
        fieldParams.add(Value.ofReference(
                entityName + "::" + GETTER_METHOD_PREFIX + ucfirst(fieldName)));

        // Add optional Attribute Converter
        try {
            final Convert annotation = field.getAnnotation(Convert.class);
            if (annotation != null) {
                annotation.converter();
            } else {
                fieldParams.add(Value.ofReference("null"));
            }
        } catch(MirroredTypeException e) {
            if (e.getTypeMirror() != null) {
                SimpleType converterType = SimpleType.create(e.getTypeMirror().toString());
                clazz.add(Import.of(converterType));
                fieldParams.add(Value.ofReference(shortName(converterType.getTypeName()) + ".class"));
            } else {
                throw new FieldGeneratorProcessorException("Could not fetch the designated converter for field " + fieldName + " in entity " + entityName);
            }
        }

        if (Enum.class.isAssignableFrom(fieldClass)) {
            String fieldTypeName = shortName(fieldType(field).getTypeName());
            String enumToDbTypeFunction;
            String dbTypeToEnumFunction;

            Enumerated enumerated = field.getAnnotation(Enumerated.class);
            if (enumerated != null && enumerated.value() == EnumType.STRING) {
                dbTypeToEnumFunction = fieldTypeName + "::valueOf";
                enumToDbTypeFunction = fieldTypeName + "::toString";
            } else {
                dbTypeToEnumFunction = "i -> " + fieldTypeName + ".values()[i]";
                enumToDbTypeFunction = fieldTypeName + "::ordinal";
            }
            // Add function from enum to database type
            fieldParams.add(Value.ofReference(enumToDbTypeFunction));

            // Add function from database type to enum
            fieldParams.add(Value.ofReference(dbTypeToEnumFunction));

            // Add enum class
            fieldParams.add(Value.ofReference(fieldTypeName + ".class"));
        } else {
            // Add the 'unique' boolean to the end for all field but enum
            fieldParams.add(Value.ofBoolean(col != null && col.unique()));
        }

        clazz.add(Field.of(fieldName, referenceType)
                .public_().static_().final_()
                .set(Value.ofInvocation(
                        referenceType,
                        "create",
                        fieldParams.toArray(new Value<?>[0])
                ))
                .set(Javadoc.of(
                        "This Field corresponds to the {@link " + entityName + "} field that can be obtained using the "
                                + "{@link " + entityName + "#get" + ucfirst(fieldName) + "()} method."
                )));
    }

    private Type referenceType(Element field, java.lang.Class fieldClass, String entityName) throws FieldGeneratorProcessorException {
        Type fieldType = fieldType(field);
        Type dbType = dbType(field);
        Type entityType = SimpleType.create(entityName);
        final Type type;

        try {
            if (fieldClass.isPrimitive()) {
                type = primitiveFieldType(fieldType, entityType, fieldClass);
            } else if (Enum.class.isAssignableFrom(fieldClass)) {
                type = enumFieldType(field, fieldType, entityType);
            } else if (Comparable.class.isAssignableFrom(fieldClass) && field.getAnnotation(Lob.class) == null) {
                type = String.class.equals(fieldClass) ?
                        SimpleParameterizedType.create(StringField.class, entityType, String.class) :
                        SimpleParameterizedType.create(ComparableField.class, entityType, dbType, fieldType);
            } else {
                type = SimpleParameterizedType.create(ReferenceField.class, entityType, dbType, fieldType);
                messager.printMessage(Diagnostic.Kind.NOTE, "Parsing field type: " + fieldType.getTypeName() + " for field " + field.getSimpleName());
            }
        } catch (UnsupportedOperationException e) {
            throw new FieldGeneratorProcessorException("Primitive type " + fieldType.getTypeName() + " could not be parsed.");
        }

        return type;
    }

    private Type enumFieldType(Element field, Type fieldType, Type entityType) {
        Type type;
        Enumerated enumerated = field.getAnnotation(Enumerated.class);
        if (enumerated != null && enumerated.value() == EnumType.STRING) {
            type = SimpleParameterizedType.create(
                    EnumField.class,
                    entityType,
                    String.class,
                    fieldType);
        } else {
            type = SimpleParameterizedType.create(
                    EnumField.class,
                    entityType,
                    Integer.class, // Default is EnumType.ORDINAL
                    fieldType);
        }
        return type;
    }

    private Type fieldType(Element field) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Using type parser to parse: " + field.asType().toString());
        return StandardTypeParser.render(field.asType().toString());
    }

    /* Returns the field database type. If no converter is used, the database type is assumed to be the same as the field type. */
    private Type dbType(Element field) {
        Optional<Type> databaseType = Optional.empty();

        final Lob lob = field.getAnnotation(Lob.class);
        final Temporal temporal = field.getAnnotation(Temporal.class);
        final Convert convert = field.getAnnotation(Convert.class);
        final Column column = field.getAnnotation(Column.class);

        if (lob != null) {
            // byte[] correspond to Blob and String correspond to Clob
            java.lang.Class<?> c = parseType(field.asType().toString());
            databaseType = (c.isArray() || c.isAssignableFrom(Blob.class)) ? Optional.of(Blob.class) : Optional.of(Clob.class);
        } else if (temporal != null) {
            databaseType = timeType(temporal.value());
        } else if (column != null
                && (column.columnDefinition().equals("TIME")
                || column.columnDefinition().equals("DATE")
                || column.columnDefinition().equals("TIMESTAMP"))) {
            databaseType = timeType(column.columnDefinition());

        } else {
            // Derive database field type from converter
            try {
                if (convert != null) {
                    convert.converter();
                }
            } catch (MirroredTypeException e) {
                Optional<SimpleType> converterType = Optional.of(SimpleType.create(e.getTypeMirror().toString()));
                java.lang.Class<?> c = parseType(converterType.get().getTypeName());
                // Stream converter methods to retrieve database type
                databaseType = Stream.of(c.getDeclaredMethods())
                        .filter(m -> m.getName().equals("convertToDatabaseColumn")
                                && !m.getReturnType().getSimpleName().equals("Object"))
                        .map(m -> (Type) SimpleType.create(m.getReturnType().getTypeName()))
                        .findFirst();
            }
        }
        return databaseType.orElse(fieldType(field));
    }

    private Optional<Type> timeType(TemporalType temporalType) {
        Objects.requireNonNull(temporalType, "Temporal type cannot be null");
        switch (temporalType) {
            case DATE:
                return Optional.of(java.sql.Date.class);
            case TIME:
                return Optional.of(java.sql.Time.class);
            case TIMESTAMP:
                return Optional.of(java.sql.Timestamp.class);
            default:
                throw new FieldGeneratorProcessorException("Unknown temporal type " + temporalType);
        }
    }

    private Optional<Type> timeType(String columnDefinition) {
        Objects.requireNonNull(columnDefinition, "Column definition type cannot be null");
        switch (columnDefinition) {
            case "DATE":
                return Optional.of(java.sql.Date.class);
            case "TIME":
                return Optional.of(java.sql.Time.class);
            case "TIMESTAMP":
                return Optional.of(java.sql.Timestamp.class);
            default:
                throw new FieldGeneratorProcessorException("Cannot process information about database time type from columnDefinition: " +  columnDefinition);
        }
    }

    private Type primitiveFieldType(Type fieldType, Type entityType, java.lang.Class c) throws UnsupportedOperationException {
        Type type;
        switch (c.getSimpleName()) {
            case "int":
                type = SimpleParameterizedType.create(
                        IntField.class,
                        entityType,
                        Integer.class
                );
                break;
            case "byte":
                type = SimpleParameterizedType.create(
                        ByteField.class,
                        entityType,
                        Byte.class
                );
                break;
            case "short":
                type = SimpleParameterizedType.create(
                        ShortField.class,
                        entityType,
                        Short.class
                );
                break;
            case "long":
                type = SimpleParameterizedType.create(
                        LongField.class,
                        entityType,
                        Long.class
                );
                break;
            case "float":
                type = SimpleParameterizedType.create(
                        FloatField.class,
                        entityType,
                        Float.class
                );
                break;
            case "double":
                type = SimpleParameterizedType.create(
                        DoubleField.class,
                        entityType,
                        Double.class
                );
                break;
            case "char":
                type = SimpleParameterizedType.create(
                        CharField.class,
                        entityType,
                        Character.class
                );
                break;
            case "boolean":
                type = SimpleParameterizedType.create(
                        BooleanField.class,
                        entityType,
                        Boolean.class
                );
                break;
            default : throw new UnsupportedOperationException(
                    "Unknown primitive type: '" + fieldType.getTypeName() + "'."
            );
        }
        return type;
    }

}