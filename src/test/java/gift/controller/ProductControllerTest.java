package gift.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.LoginRequest;
import gift.dto.ProductRequest;
import gift.dto.ProductResponse;
import gift.model.MemberRole;
import gift.service.ProductService;
import gift.service.auth.AuthService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private ProductService productService;
    private String managerToken;
    private String memberToken;

    @BeforeEach
    @DisplayName("관리자, 이용자의 토큰 값 세팅하기")
    void setBaseData() {
        var managerLoginRequest = new LoginRequest("admin@naver.com", "password");
        managerToken = authService.login(managerLoginRequest).token();
        var memberLoginRequest = new LoginRequest("member@naver.com", "password");
        memberToken = authService.login(memberLoginRequest).token();
    }

    @Test
    @DisplayName("잘못된 가격으로 된 오류 상품 생성하기")
    void addProductFailWithPrice() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("상품1", -1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("금액은 0보다 크거나 같아야 합니다."));
    }

    @Test
    @DisplayName("이름의 길이가 15초과인 오류 상품 생성하기")
    void addProductFailWithNameLength() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("햄버거햄버거햄버거햄버거햄버거햄", 1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("이름의 길이는 15자를 초과할 수 없습니다."));
    }

    @Test
    @DisplayName("카카오를 포함한 이름을 가진 오류 상품 생성하기")
    void addProductFailWithNameKAKAO() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("카카오456", 1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("카카오가 포함된 문구는 담당 MD와 협의한 경우에만 사용할 수 있습니다."));
    }

    @Test
    @DisplayName("카카오를 포함한 이름을 가진 상품 매니저로 생성하기")
    void addProductSuccessWithNameKAKAO() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + managerToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("카카오456", 1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        result.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("빈 이름을 가진 오류 상품 생성하기")
    void addProductFailWithEmptyName() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("", 1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("이름의 길이는 최소 1자 이상이어야 합니다."));
    }

    @Test
    @DisplayName("정상 상품 생성하기 - 특수문자 포함")
    void addProductSuccessWithSpecialChar() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("햄버거()[]+-&/_", 1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        var createdResult = result.andExpect(status().isCreated()).andReturn();

        var location = createdResult.getResponse().getHeader("Location");
        var productId = location.replaceAll("/api/products/", "");
        deleteProduct(Long.parseLong(productId));
    }

    @Test
    @DisplayName("정상 상품 생성하기 - 공백 포함")
    void addProductSuccessWithEmptySpace() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("햄버거 햄버거 햄버거", 1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        result.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("오류 상품 생성하기 - 허용되지 않은 특수문자 포함")
    void addProductFailWithSpecialChar() throws Exception {
        //given
        var postRequest = post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken)
                .content(objectMapper.writeValueAsString(new ProductRequest("햄버거()[]+-&/_**", 1000, "이미지 주소", 1L)));
        //when
        var result = mockMvc.perform(postRequest);
        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("허용되지 않은 형식의 이름입니다."));
    }

    @Test
    @DisplayName("11개의 상품을 등록하였을 때, 2번째 페이지의 조회의 결과는 1개의 상품만을 반환한다.")
    void getProductsWithPageable() throws Exception {
        //given
        var productRequest = new ProductRequest("햄버거()[]+-&/_**", 1000, "이미지 주소", 1L);
        for (int i = 0; i < 11; i++) {
            productService.addProduct(productRequest, MemberRole.MEMBER);
        }
        var getRequest = get("/api/products?page=1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken);
        //when
        var getResult = mockMvc.perform(getRequest);
        //then
        var productResult = getResult.andExpect(status().isOk()).andReturn();
        var productListString = productResult.getResponse().getContentAsString();
        var productResponseList = objectMapper.readValue(productListString, new TypeReference<List<ProductResponse>>() {
        });
        Assertions.assertThat(productResponseList.size()).isEqualTo(4);

        deleteProducts(productResponseList);
    }

    @Test
    @DisplayName("잘못된 정렬 데이터가 올 경우 예외를 던진다.")
    void getProductsInvalidPageSort() throws Exception {
        //given
        var getRequest = get("/api/products?sort=name,desc")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken);
        //when
        var getResult = mockMvc.perform(getRequest);
        //then
        getResult.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 크기 데이터가 올 경우 예외를 던진다.")
    void getProductsInvalidPageSize() throws Exception {
        //given
        var getRequest = get("/api/products?size=30")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + memberToken);
        //when
        var getResult = mockMvc.perform(getRequest);
        //then
        getResult.andExpect(status().isBadRequest());
    }

    private void deleteProduct(Long id) {
        productService.deleteProduct(id);
    }

    private void deleteProducts(List<ProductResponse> productResponseList) {
        for (var product : productResponseList) {
            productService.deleteProduct(product.id());
        }
    }
}
