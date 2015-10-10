package com.mantralabsglobal.addtobill;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;

import com.mantralabsglobal.addtobill.model.User;
import com.mantralabsglobal.addtobill.repository.UserRepository;

@SpringBootApplication
@PropertySource("classpath:/com/mantralabsglobal/addtobill/config/application.properties")
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        UserRepository respository = context.getBean(UserRepository.class);
        User user = respository.findOneByEmail("admin");
        if(user == null)
        {
        	//Create admin user
        	user = new User();
        	user.setEmail("admin");
        	user.setPassword("admin");
        	user.setRoles( Arrays.asList("ROLE_ADMIN"));
        	respository.save(user);
        }
    }
}
