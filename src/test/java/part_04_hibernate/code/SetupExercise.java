package part_04_hibernate.code;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.*;
import javax.sql.DataSource;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SetupExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class SetupExercise {

    @Autowired
    private SessionFactory sessionFactory;


    @Test
    public void exercise_setupSessionFactory() {
        assertNotNull(sessionFactory);
        System.out.println("We have a SessionFactory, yay!");
    }


    /**
     * The only entity/table we will have in our database
     */
    @Entity
    @Table(name = "EVENTS")
    public class Event {

        @Id
        @GeneratedValue
        private Long id;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "EVENT_DATE")
        private Date date;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    // our spring java-config
    @Configuration
    public static class MySpringConfig {

        @Bean
        public DataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("sa");
            return ds;
        }

        @Bean
        /**
         * 1. There are different LocaLSessionFactoryBeans, depending on
         * which Hibernate version you are using (3.x, 4.x, 5.x)
         * 2. Make sure to configure Hibernate with the correct database
         * dialect
         * 3. We let Hibernate auto-create our database here
         */
        public LocalSessionFactoryBean sessionFactory() {
            LocalSessionFactoryBean result =
                    new LocalSessionFactoryBean();

            // set our datasource
            result.setDataSource(dataSource());

            // all the mappings we want hibernate to know
            result.setAnnotatedClasses(Event.class);

            // properties: dialect, auto-creation of database etc.
            Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty(Environment.DIALECT,
                    H2Dialect.class.getName());
            /*
            hibernateProperties.setProperty(Environment.HBM2DDL_AUTO,
                    "create-drop");
             */

            /*
            hibernateProperties.setProperty(Environment.SHOW_SQL,"true");
            hibernateProperties.setProperty(Environment.FORMAT_SQL,"true");
             */

            result.setHibernateProperties(hibernateProperties);

            return result;
        }


        @Bean
        public PlatformTransactionManager txManager() {
            return new HibernateTransactionManager(sessionFactory()
                    .getObject());
        }
    }
}
