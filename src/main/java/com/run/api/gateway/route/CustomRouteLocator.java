package com.run.api.gateway.route;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.run.gateway.entity.ZuulRouteVO;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator {

	public final static Logger	logger	= LoggerFactory.getLogger(CustomRouteLocator.class);

	private JdbcTemplate		jdbcTemplate;

	private ZuulProperties		properties;



	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}



	public CustomRouteLocator(String servletPath, ZuulProperties properties) {
		super(servletPath, properties);
		this.properties = properties;
		logger.info("servletPath:{}", servletPath);
	}

	// 父类已经提供了这个方法，这里写出来只是为了说明这一个方法很重要！！！
	// @Override
	// protected void doRefresh() {
	// super.doRefresh();
	// }



	@Override
	public void refresh() {
		doRefresh();
	}



	@Override
	protected Map<String, ZuulRoute> locateRoutes() {
		LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
		// 从application.properties中加载路由信息
		routesMap.putAll(super.locateRoutes());
		// 从db中加载路由信息
		routesMap.putAll(locateRoutesFromDB());
		// 优化一下配置
		LinkedHashMap<String, ZuulRoute> values = new LinkedHashMap<>();
		for (Map.Entry<String, ZuulRoute> entry : routesMap.entrySet()) {
			String path = entry.getKey();
			// Prepend with slash if not already present.
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			if (StringUtils.contains(this.properties.getPrefix(), "")) {
				path = this.properties.getPrefix() + path;
				if (!path.startsWith("/")) {
					path = "/" + path;
				}
			}
			values.put(path, entry.getValue());
		}
		return values;
	}



	private Map<String, ZuulRoute> locateRoutesFromDB() {
		try {
			Map<String, ZuulRoute> routes = new LinkedHashMap<>();
			List<ZuulRouteVO> results = jdbcTemplate.query("select * from gateway_api_define where enabled = true ",
					new BeanPropertyRowMapper<>(ZuulRouteVO.class));
			
			//根据路径长度排序,以便于从长到短匹配
			results.sort(new Comparator<ZuulRouteVO>() {
				@Override
				public int compare(ZuulRouteVO zr1, ZuulRouteVO zr2) {
					if (zr1.getPath().length() < zr2.getPath().length()) {
						return 1;
					} else if (zr1.getPath().length() == zr2.getPath().length()) {
						return 0;
					} else {
						return -1;
					}
				}

			});

			for (ZuulRouteVO result : results) {
				if (StringUtils.isBlank(result.getPath()) || StringUtils.isBlank(result.getServiceId())) {
					continue;
				}

				ZuulRoute zuulRoute = new ZuulRoute();
				BeanUtils.copyProperties(result, zuulRoute);
				routes.put(zuulRoute.getPath(), zuulRoute);
			}
			return routes;
		} catch (Exception e) {
			logger.error("=============load zuul route info from db with error==============", e);
		}
		return null;
	}

}
