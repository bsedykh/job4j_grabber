package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    private static Properties getConfig() throws IOException {
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            return config;
        }
    }

    private static Connection getConnection(Properties config) throws Exception {
        Class.forName(config.getProperty("driver-class-name"));
        return DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        );
    }

    public static void main(String[] args) {
        try {
            Properties config = getConfig();
            try (Connection connection = getConnection(config)) {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("connection", connection);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(
                                Integer.parseInt(config.getProperty("rabbit.interval"))
                        )
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Job started");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "INSERT INTO rabbit(created_date) VALUES (?)"
                         )) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
                System.out.println("Job finished");
            } catch (SQLException e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
