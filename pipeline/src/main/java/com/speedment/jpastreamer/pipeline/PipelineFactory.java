package com.speedment.jpastreamer.pipeline;

public interface PipelineFactory {

     /**
      * Creates and returns a new Pipeline.
      *
      * @param rootClass the entity class to be used as a root (source)
      * @author     Per Minborg
      */
     <T> Pipeline<T> createPipeline(Class<T> rootClass);

}