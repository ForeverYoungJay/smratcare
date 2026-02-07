package com.zhiyangyun.care.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

@Configuration
public class FactoryBeanObjectTypeFix {
  @Bean
  public static BeanFactoryPostProcessor factoryBeanObjectTypePostProcessor() {
    return new BeanFactoryPostProcessor() {
      @Override
      public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
          throws BeansException {
        String[] names = beanFactory.getBeanDefinitionNames();
        for (String name : names) {
          BeanDefinition definition = beanFactory.getBeanDefinition(name);
          Object attr = definition.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE);
          if (attr instanceof String attrClassName) {
            try {
              Class<?> clazz = ClassUtils.forName(attrClassName, beanFactory.getBeanClassLoader());
              definition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, clazz);
            } catch (ClassNotFoundException ex) {
              // Keep original attribute if class cannot be resolved.
            }
          }
        }
      }
    };
  }
}
