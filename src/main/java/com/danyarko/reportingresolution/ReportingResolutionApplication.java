package com.danyarko.reportingresolution;

import com.danyarko.reportingresolution.component.LoadWorkbookComponent;
import com.danyarko.reportingresolution.hibernate.HibernateUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
public class ReportingResolutionApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ReportingResolutionApplication.class, args);

        LoadWorkbookComponent loadWorkbookComponent = (LoadWorkbookComponent) applicationContext.getBean("LoadWorkbookComponent");
        loadWorkbookComponent.loadWorkbook();

        HibernateUtil.closeSessionFactory();
    }

}
