package com.core.constella;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

import com.core.constella.api.diary.service.DiaryService;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class ConstellaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConstellaApplication.class, args);
	}

	@Bean
    public CommandLineRunner run(DiaryService diaryService) {
        return args -> {
            diaryService.updateAllDiariesWithCountryLatLng();
            System.out.println("모든 Diary의 위도/경도 업데이트 완료!");
        };
    }

}
