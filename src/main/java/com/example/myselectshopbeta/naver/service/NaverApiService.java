package com.example.myselectshopbeta.naver.service;

import com.example.myselectshopbeta.naver.dto.ItemDto;
import org.springframework.beans.factory.annotation.Value; // Lombok의 Value를 쓰는게 아닌데 왜 ㅆㅂ
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@PropertySource("classpath:NaverAPI.properties")

public class NaverApiService {
    @Value("${api.naver.id}")
    private String apiID;
    @Value("${api.naver.secret}")
    private String apiSECRET;

    public List<ItemDto> searchItems(String query) {
        // 1. Rest Template 선언
        // 스프링에서 제공하는 http 통신에 유용하게 쓸 수 있는 템플릿
        // Spring 3부터 지원 되었고 REST API 호출이후 응답을 받을 때까지 기다리는 동기방식이다
        RestTemplate rest = new RestTemplate();

        // 2. Naver API에 요청을 보내기 위해 Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", apiID);
        headers.add("X-Naver-Client-Secret", apiSECRET);

        // 3. Naver API에 요청을 보내기 위해 Body 설정
        String body = "";

        // 4. Header와 Body를 조립해 Http Entity 구성
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        // 5. URL에 GET 방식으로 Http Entity(Request)를 헤더에 포함해 요청하고,URI(고유식별자) 반환값은 String으로 Body에 받는다.
        ResponseEntity<String> responseEntity = rest.exchange("https://openapi.naver.com/v1/search/shop.json?display=15&query="
                + query , HttpMethod.GET, requestEntity, String.class);

        // 6. 실행 후 Log를 저장(API 정상 동작 여부)
        HttpStatus httpStatus = (HttpStatus) responseEntity.getStatusCode();
        int status = httpStatus.value();
        log.info("NAVER API Status Code : " + status);

        // 7. ResponseEntity의 Body 값을 from Json to Items에 전달한다.
        return fromJSONtoItems(responseEntity.getBody());
    }

    public List<ItemDto> fromJSONtoItems(String response) {
        // 1. JSON Object 생성 (lastBuildDate, total, start, display 등 정보가 포함된 상태)
        JSONObject rjson = new JSONObject(response);

        // 2. JSOM Array를 만들고 getJSONArray를 통해 Item을 기준으로 자른다.
        JSONArray items  = rjson.getJSONArray("items");

        // 3. ItemDto 생성
        List<ItemDto> itemDtoList = new ArrayList<>();

        // 4. JSON Array의 값을 getJSONObject를 통해 받고, 이를 ItemDTO 형식으로 변환. 그 후에 ItemDto List에 추가
        for (int i=0; i<items.length(); i++) {
            ItemDto itemDto = new ItemDto(items.getJSONObject(i));
            itemDtoList.add(itemDto);
        }

        // 5. ItemDtoList 반환
        return itemDtoList;
    }
}