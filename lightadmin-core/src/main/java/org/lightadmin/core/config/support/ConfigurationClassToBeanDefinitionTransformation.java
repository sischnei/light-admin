package org.lightadmin.core.config.support;

import com.google.common.base.Function;
import org.lightadmin.core.annotation.Administration;
import org.lightadmin.core.config.DomainTypeAdministrationConfiguration;
import org.lightadmin.core.view.DefaultScreenContext;
import org.lightadmin.core.view.ScreenContext;
import org.lightadmin.core.view.support.Fragment;
import org.lightadmin.core.view.support.FragmentBuilder;
import org.lightadmin.core.view.support.TableFragmentBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public class ConfigurationClassToBeanDefinitionTransformation implements Function<Class<?>, BeanDefinition> {

	@Override
	public BeanDefinition apply( final Class<?> dslConfiguration ) {
		Assert.notNull( dslConfiguration );

		final Administration administrationAnnotation = findAnnotation( dslConfiguration, Administration.class );

		Assert.notNull( administrationAnnotation );

		return domainTypeAdministrationConfigBeanDefinition( administrationAnnotation.value(), dslConfiguration );
	}

	private BeanDefinition domainTypeAdministrationConfigBeanDefinition( final Class<?> domainType, final Class<?> configurationClass ) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition( DomainTypeAdministrationConfiguration.class );

		builder.addConstructorArgValue( domainType );
		builder.addConstructorArgReference( repositoryBeanName( domainType ) );

		builder.addPropertyValue( "listViewFragment", listViewFragment( configurationClass ) );
		builder.addPropertyValue( "screenContext", screenContext( configurationClass ) );

		return builder.getBeanDefinition();
	}

	private Fragment listViewFragment( final Class<?> configurationClass ) {
		final Method method = ClassUtils.getMethodIfAvailable( configurationClass, "listView", FragmentBuilder.class );

		FragmentBuilder fragmentBuilder = new TableFragmentBuilder();
		if ( method != null ) {
			return ( Fragment ) ReflectionUtils.invokeMethod( method, null, fragmentBuilder );
		}

		return fragmentBuilder.build();
	}

	private ScreenContext screenContext( final Class<?> configurationClass ) {
		final Method method = ClassUtils.getMethodIfAvailable( configurationClass, "configureScreen", ScreenContext.class );

		ScreenContext screenContext = new DefaultScreenContext();
		if ( method != null ) {
			ReflectionUtils.invokeMethod( method, null, screenContext );
		}

		return screenContext;
	}

	private String repositoryBeanName( final Class<?> domainType ) {
		return StringUtils.uncapitalize( domainType.getSimpleName() ) + "Repository";
	}
}