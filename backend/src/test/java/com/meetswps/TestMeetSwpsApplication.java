package com.meetswps;

import org.springframework.boot.SpringApplication;

public class TestMeetSwpsApplication {

	public static void main(String[] args) {
		SpringApplication.from(MeetSwpsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
