package se.sundsvall.casestatus.service.scheduler.cache;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContextUtil implements ApplicationContextAware {

	private static ApplicationContext context;

	public static <T> T getBean(final Class<T> beanClass) {
		return context.getBean(beanClass);
	}

	private static synchronized void setContext(final ApplicationContext context) {
		ContextUtil.context = context;
	}

	@Override
	public void setApplicationContext(final @NotNull ApplicationContext context) {
		setContext(context);
	}
}
