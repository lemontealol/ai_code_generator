package com.yuaicodemother;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

/**
 * @author 六味lemontea 2026-01-16
 * @version 1.0
 * @description
 */
public class Codegen {
    private static final String[] generateTables = {"chat_history"};

    public static void main(String[] args) {
        //配置数据源
        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String,Object> datasource = dict.getByPath("spring.datasource");
        String url = String.valueOf(datasource.get("url"));
        String username = String.valueOf(datasource.get("username"));
        String password = String.valueOf(datasource.get("password"));
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle1();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle1() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("com.yuaicodemother.gene_result");

//        设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
//                .setTablePrefix("tb_")
                .setGenerateTable(generateTables)
                .setLogicDeleteColumn("isDelete");

        //生成service。
        globalConfig.enableService();
        //生成serviceImpl。
        globalConfig.enableServiceImpl();
        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        //设置生成mapperXml。
        globalConfig.enableMapperXml();
        //可以单独配置某个列
  /*      ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setColumnName("tenant_id");
        columnConfig.setLarge(true);
        columnConfig.setVersion(true);
        globalConfig.getStrategyConfig()
                .setColumnConfig("tb_account", columnConfig);*/
        globalConfig.getJavadocConfig()
                .setAuthor("六味lemontea")
                .setSince("2026-01-16");
//                .setTableCommentFormat()
//                .setColumnCommentFormat();
        return globalConfig;
    }
}