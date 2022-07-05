package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.UserController;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class UserControllerTest {

    @Test
    @DisplayName("유저 타입 패치 성공시, 결과 반환 하는지 확인")
    public void testIfUserTypePatchSucceedReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(false, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        // when
        UserController userController = new UserController(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository
        );
        CommonResponse<UserDTO> result = userController.patchUserType(user, userTypeRequest);

        // then
        user.setIsKid(false);
        user.setIsFemale(false);
        UserDTO userDTO = new UserDTO(user);
        Assertions.assertEquals(CommonResponse.onSuccess(userDTO), result);
    }

    @Test
    @DisplayName("body 없어서 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithoutArgumentsThrowNullPointerException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(false, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        // when
        UserController userController = new UserController(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository
        );

        // then
        Assertions.assertThrows(NullPointerException.class, () -> {
            userController.patchUserType(user, null);
        });
    }

    @Test
    @DisplayName("인증 유저 없어서 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithoutValidUserThrowBadRequestException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(false, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(null));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        // when
        UserController userController = new UserController(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.patchUserType(user, userTypeRequest);
        });
    }

    @Test
    @DisplayName("자녀 지정시, 자녀 row 생성 확인")
    public void testIfKidInsertSucceedWhenUserTypePatchSucceed() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        Kid kid = Kid.builder()
            .savings(0L)
            .user(user)
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        Mockito.when(mockUserRepository.save(user)).thenReturn(user);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        Mockito.when(mockKidRepository.save(kid)).thenReturn(kid);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        // when
        UserController userController = new UserController(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository
        );
        CommonResponse result = userController.patchUserType(user, userTypeRequest);

        // then
        user.setIsFemale(false);
        user.setIsKid(true);
        UserDTO userDTO = new UserDTO(user);

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockUserRepository, Mockito.times(1)).save(uCaptor.capture());
        Mockito.verify(mockKidRepository, Mockito.times(1)).save(kCaptor.capture());

        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(kid, kCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(userDTO), result);
    }

    @Test
    @DisplayName("부모 지정시, 부모 row 생성 확인")
    public void testIfParentInsertSucceedWhenUserTypePatchSucceed() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        Parent parent = Parent.builder()
            .user(user)
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(true, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        Mockito.when(mockUserRepository.save(user)).thenReturn(user);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        Mockito.when(mockParentRepository.save(parent)).thenReturn(parent);

        // when
        UserController userController = new UserController(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository
        );
        CommonResponse result = userController.patchUserType(user, userTypeRequest);

        // then
        user.setIsFemale(true);
        user.setIsKid(false);
        UserDTO userDTO = new UserDTO(user);

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockUserRepository, Mockito.times(1)).save(uCaptor.capture());
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());

        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(parent, pCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(userDTO), result);
    }
}
