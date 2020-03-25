package com.dejavu.swagger;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ServiceDescriptionUpdater {
	private static final String DEFAULT_SWAGGER_URL = "/v2/api-docs";
	private static final String KEY_SWAGGER_URL = "swagger_url";
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	private final RestTemplate template;

	public ServiceDescriptionUpdater() {
		this.template = new RestTemplate();
	}

	@Autowired
	private ServiceDefinitionsContext definitionContext;

	@Scheduled(fixedDelayString = "${swagger.config.refreshrate}")
	public void refreshSwaggerConfigurations() {
		discoveryClient.getServices().stream().forEach(serviceId -> {
			List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);
			if (serviceInstances == null || serviceInstances.isEmpty()) { // Should not be the case kept for failsafe
				System.out.println("serviceInstances is null");
			} else {
				ServiceInstance instance = serviceInstances.get(0);
				String swaggerURL = getSwaggerURL(instance);
				Optional<Object> jsonData = getSwaggerDefinitionForAPI(serviceId, swaggerURL);
				if (jsonData.isPresent()) {
					String content = getJSON(serviceId, jsonData.get());
					definitionContext.addServiceDefinition(serviceId, content);
				} else {
				}
			}
		});
	}

	private String getSwaggerURL(ServiceInstance instance) {
		String swaggerURL = instance.getMetadata().get(KEY_SWAGGER_URL);
		System.out.println("swaggerURL "+swaggerURL);
		return swaggerURL != null ? instance.getUri() + swaggerURL : instance.getUri() + DEFAULT_SWAGGER_URL;
	}

	private Optional<Object> getSwaggerDefinitionForAPI(String serviceName, String url) {
		try {
			Object jsonData = template.getForObject(url, Object.class);
			return Optional.of(jsonData);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return Optional.empty();
		}
	}

	public String getJSON(String serviceId, Object jsonData) {
		try {
			return new ObjectMapper().writeValueAsString(jsonData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
