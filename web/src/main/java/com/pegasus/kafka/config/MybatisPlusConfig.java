package com.pegasus.kafka.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.service.property.PropertyService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Mybatis's config for set the paging.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Configuration
public class MybatisPlusConfig {
    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    private final PropertyService propertyService;

    public MybatisPlusConfig(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        return new PaginationInnerInterceptor();
    }

    @Bean
    public DataSource dataSource() {
        return Common.createDataSource(propertyService.getDbHost(),
                propertyService.getDbPort().toString(),
                propertyService.getDbName(),
                propertyService.getDbUsername(),
                propertyService.getDbPassword());
    }

    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        initDatabase();
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);

        sqlSessionFactory.setMapperLocations(resourceResolver.getResources("classpath*:/com.pegasus.kafka.mapper/*Mapper.xml"));
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(true);
        //这个配置会将执行的sql打印出来，在开发或测试的时候可以用
        //configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        configuration.setCacheEnabled(false);
        sqlSessionFactory.setConfiguration(configuration);
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor());
        sqlSessionFactory.setPlugins(mybatisPlusInterceptor);
        return sqlSessionFactory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    private void initDatabase() {
        try {
            try (DruidDataSource dataSource = (DruidDataSource) Common.createDataSource(propertyService.getDbHost(),
                    propertyService.getDbPort().toString(),
                    "",
                    propertyService.getDbUsername(),
                    propertyService.getDbPassword(),
                    1,
                    1,
                    1);
                 Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("CREATE DATABASE IF NOT EXISTS %s CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", propertyService.getDbName()))) {
                preparedStatement.execute();
            }
        } catch (Exception ignored) {

        }
    }
}