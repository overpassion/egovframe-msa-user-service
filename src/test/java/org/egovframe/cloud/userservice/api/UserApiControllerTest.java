package org.egovframe.cloud.userservice.api;

import org.egovframe.cloud.userservice.api.user.dto.UserResponseDto;
import org.egovframe.cloud.userservice.api.user.dto.UserSaveRequestDto;
import org.egovframe.cloud.userservice.api.user.dto.UserUpdateRequestDto;
import org.egovframe.cloud.userservice.domain.user.User;
import org.egovframe.cloud.userservice.domain.user.UserRepository;
import org.egovframe.cloud.userservice.service.user.UserService;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class UserApiControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8000/user-service";
    private static final String TEST_COM = "@test.com";
    private static final String TEST_EMAIL = System.currentTimeMillis() + TEST_COM;
    private static final String TEST_PASSWORD = "test1234!";

    @Test
    @Order(Integer.MAX_VALUE)
    public void cleanup() throws Exception {
        // 테스트 후 데이터 삭제
        List<User> users = userRepository.findByEmailContains("test.com");
        users.forEach(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void 사용자_등록된다() throws Exception {
        // given
        UserSaveRequestDto userSaveRequestDto = UserSaveRequestDto.builder()
                .userName("사용자")
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        userService.save(userSaveRequestDto);

        // when
        UserResponseDto findUser = userService.findByEmail(TEST_EMAIL);

        // then
        assertThat(findUser.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @Order(1)
    public void 사용자_API호출_등록된다() throws Exception {
        // given
        UserSaveRequestDto userSaveRequestDto = UserSaveRequestDto.builder()
                .userName("사용자1")
                .email(System.currentTimeMillis() + TEST_COM)
                .password(TEST_PASSWORD)
                .build();

        String url = USER_SERVICE_URL + "/api/v1/users";

        // when
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, userSaveRequestDto, Long.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(2)
    public void 사용자_수정된다() throws Exception {
        // given
        UserResponseDto findUser = userService.findByEmail(TEST_EMAIL);
        UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
                .userName("사용자수정")
                .email(TEST_EMAIL)
                .build();

        // when
        userService.update(findUser.getUserId(), userUpdateRequestDto);
        UserResponseDto updatedUser = userService.findByEmail(TEST_EMAIL);

        // then
        assertThat(updatedUser.getUserName()).isEqualTo("사용자수정");
    }

    @Test
    public void 사용자_등록오류() throws Exception {
        // given
        UserSaveRequestDto userSaveRequestDto = UserSaveRequestDto.builder()
                .userName("사용자")
                .email("email")
                .password("test")
                .build();

        String url = USER_SERVICE_URL + "/api/v1/users";

        RestClientException restClientException = Assertions.assertThrows(RestClientException.class, () -> {
            restTemplate.postForEntity(url, userSaveRequestDto, Long.class);
        });
        System.out.println("restClientException.getMessage() = " + restClientException.getMessage());
    }

    @Test
    public void 사용자_로그인된다() throws Exception {
        // given
        JSONObject loginJson = new JSONObject();
        loginJson.put("email", TEST_EMAIL);
        loginJson.put("password", TEST_PASSWORD);

        String url = USER_SERVICE_URL + "/login";

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, loginJson.toString(), String.class);
        responseEntity.getHeaders().entrySet().forEach(System.out::println);
        assertThat(responseEntity.getHeaders().containsKey("access-token")).isTrue();
    }

    @Test
    public void 사용자_로그인_오류발생한다() throws Exception {
        // given
        JSONObject loginJson = new JSONObject();
        loginJson.put("email", TEST_EMAIL);
        loginJson.put("password", "test");

        String url = USER_SERVICE_URL + "/login";

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, loginJson.toString(), String.class);
        System.out.println("responseEntity = " + responseEntity);
        assertThat(responseEntity.getHeaders().containsKey("access-token")).isFalse();
    }

}