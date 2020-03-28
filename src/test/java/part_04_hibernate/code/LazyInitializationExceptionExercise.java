package part_04_hibernate.code;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = LazyInitializationExceptionExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class LazyInitializationExceptionExercise {

    @Autowired
    private SessionFactory sessionFactory;


    @Test(expected = LazyInitializationException.class)
    public void exercise_trigger_lazyInitializationException() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        // lets get mrs president added
        Mother mother = new Mother("Michelle");
        session.save(mother);

        // and one of her kids
        Kid malia = new Kid("Malia");
        session.save(malia);

        // and the other one too
        Kid sasha = new Kid("Sasha");
        session.save(sasha);

        // oops, we forgot to add the kids to her mother
        mother.addKid(malia);
        mother.addKid(sasha);

        // .. do other stuff
        session.getTransaction().commit();
        session.close();


        // let some time pass...and we open up a new database connection

        session = sessionFactory.openSession();
        // we load it from the database
        mother = session.get(Mother.class, mother.getId());
        System.out.println("Did our query also load the kids? : " +
                Hibernate.isInitialized(mother.getKids()));
        session.close();

        // this will throw our famous LazyInitializationException....
        // after all, we closed our db connection but then try to access
        // the kids from the mother!
        for (Kid kid : mother.getKids()) {
            System.out.println(kid);
        }
    }


    /**
     * Our Hibernate Classes/Tables
     */
    @Entity
    @Table(name = "MOTHERS")
    public static class Mother {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @OneToMany(mappedBy = "mother")
        private Set<Kid> kids = new HashSet<>();

        public Mother() {
        }

        public Mother(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void addKid(Kid kid) {
            kids.add(kid);
            kid.setMother(this);
        }

        public Set<Kid> getKids() {
            return kids;
        }

        public void setKids(Set<Kid> kids) {
            this.kids = kids;
        }
    }


    @Entity
    @Table(name = "KIDS")
    public static class Kid {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @ManyToOne
        private Mother mother;

        public Kid() {
        }

        public Kid(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Mother getMother() {
            return mother;
        }

        public void setMother(Mother mother) {
            this.mother = mother;
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
            result.setDataSource(dataSource());
            result.setAnnotatedClasses(Mother.class, Kid.class);
            Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty(Environment.DIALECT,
                    H2Dialect.class.getName());
            hibernateProperties.setProperty(Environment.HBM2DDL_AUTO,
                    "create-drop");
            hibernateProperties.setProperty(Environment.SHOW_SQL, "true");
            hibernateProperties.setProperty(Environment.FORMAT_SQL, "true");
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
