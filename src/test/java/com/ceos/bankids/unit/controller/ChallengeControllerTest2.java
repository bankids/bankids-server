package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.controller.ChallengeController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.AbstractTimestamp;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Comment;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Notification;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.mapper.ChallengeMapper;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.NotificationRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.ChallengeUserServiceImpl;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class ChallengeControllerTest2 {

    // Enum ChallengeStatus
    private static final ChallengeStatus pending = ChallengeStatus.PENDING;
    private static final ChallengeStatus walking = ChallengeStatus.WALKING;
    private static final ChallengeStatus achieved = ChallengeStatus.ACHIEVED;
    private static final ChallengeStatus failed = ChallengeStatus.FAILED;
    private static final ChallengeStatus rejected = ChallengeStatus.REJECTED;

    @Test
    @DisplayName("돈길 생성하기 요청 시, 정상 response 확인")
    public void postChallengeReqSuccessResponseTest() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            challengeRequest.getChallengeCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(challengeRequest.getItemName()))
            .thenReturn(newTargetItem);
        Mockito.when(mockFamilyUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(Optional.of(familyUser));
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, kidUser))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = null;
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        // then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.postChallenge(
            kidUser, challengeRequest);
        // response 데이터 검증
        Assertions.assertEquals(challengeDTOCommonResponse.getData(), challengeDTO);
        // 만들어진 돈길 status 검증
        Assertions.assertEquals(challengeDTOCommonResponse.getData().getChallengeStatus(), pending);
        // 부모의 totalRequest + 1 검증
        Assertions.assertEquals(parent.getTotalRequest(), 1L);
    }

    @Test
    @DisplayName("돈길 생성하기 요청 성공 시, challengeUser row 정상 생성 검증")
    public void postChallengeIfSuccessMakeChallengeUserRowTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);

        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            challengeRequest.getChallengeCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(challengeRequest.getItemName()))
            .thenReturn(newTargetItem);
        Mockito.when(mockFamilyUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(Optional.of(familyUser));
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, kidUser))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = null;
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        // then
        challengeController.postChallenge(kidUser, challengeRequest);
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).save(cuCaptor.capture());

        Assertions.assertEquals(newChallenge, cuCaptor.getValue().getChallenge());
    }

    @Test
    @DisplayName("돈길 생성하기 요청 시, 걷고 있는 돈길의 개수가 5개 이상이면 403 에러")
    public void postChallengeIfWalkingChallengeHigherThan5ForbiddenExceptionTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);

        //given
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge challenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge1 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge2 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge3 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge4 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(challenge)
            .member("parent").build();

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(kidUser).challenge(challenge1)
            .member("parent").build();

        ChallengeUser challengeUser2 = ChallengeUser.builder().user(kidUser).challenge(challenge2)
            .member("parent").build();

        ChallengeUser challengeUser3 = ChallengeUser.builder().user(kidUser).challenge(challenge3)
            .member("parent").build();

        ChallengeUser challengeUser4 = ChallengeUser.builder().user(kidUser).challenge(challenge4)
            .member("parent").build();

        List<ChallengeUser> challengeUserList = List.of(challengeUser, challengeUser1,
            challengeUser2, challengeUser3, challengeUser4);

        //when
        Mockito.when(mockChallengeUserRepository.findByUserIdAndChallenge_ChallengeStatus(
            kidUser.getId(), walking)).thenReturn(challengeUserList);

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = null;
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> challengeController.postChallenge(kidUser, challengeRequest));
    }

    @Test
    @DisplayName("돈길 생성 요청 시, 해당 계약 부모가 없으면 400에러")
    public void postChallengeIfNotExistContractUserBadRequestErrorTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);

        //given
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        Family family = Family.builder().code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().user(kidUser).family(family).build();
        FamilyUser familyUser1 = FamilyUser.builder().user(parentUser).family(family).build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        //when
        Mockito.when(mockFamilyUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(Optional.of(familyUser));
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, kidUser))
            .thenReturn(familyUserList);

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = null;
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        Assertions.assertThrows(BadRequestException.class,
            () -> challengeController.postChallenge(kidUser, challengeRequest));
    }

    //Todo: 돈길 삭제하기 API 테스트 코드 작성하기
    @Test
    @DisplayName("걷고 있는 돈길 삭제 요청 시 2주 경과된 돈길 삭제 정상 response 테스트")
    public void deleteWalkingChallengeSuccessResponseTest() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            newChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(3)),
            Timestamp.class
        );

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(newChallenge)
            .member("parent").build();

        Progress progress = Progress.builder().challenge(newChallenge).weeks(1L).isAchieved(true)
            .build();

        Progress progress1 = Progress.builder().challenge(newChallenge).weeks(2L).isAchieved(true)
            .build();

        List<Progress> progressList = List.of(progress, progress1);

        newChallenge.setProgressList(progressList);

        //when
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(challengeUser));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.deleteChallenge(
            kidUser, newChallenge.getId());
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());
        Assertions.assertNotEquals(null, kid.getDeleteChallenge());
        Assertions.assertEquals(challengeDTO, challengeDTOCommonResponse.getData());

    }

    @Test
    @DisplayName("돈길 삭제 요청 시, 1주가 경과되지 않은 돈길은 그냥 삭제")
    public void deleteChallengeIfNot1WeeksChallengeJustDeleteTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            newChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusDays(3)),
            Timestamp.class
        );

        kid.setDeleteChallenge(Timestamp.valueOf(LocalDateTime.now()));

//        ReflectionTestUtils.setField(
//            kid,
//            Timestamp.class,
//            "deleteChallenge",
//            Timestamp.valueOf(LocalDateTime.now()),
//            Timestamp.class
//        );

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(newChallenge)
            .member("parent").build();

        Progress progress = Progress.builder().challenge(newChallenge).weeks(1L).isAchieved(true)
            .build();

        Progress progress1 = Progress.builder().challenge(newChallenge).weeks(2L).isAchieved(true)
            .build();

        List<Progress> progressList = List.of(progress, progress1);

        newChallenge.setProgressList(progressList);

        //when
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(challengeUser));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.deleteChallenge(
            kidUser, newChallenge.getId());
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());
        Assertions.assertEquals(challengeDTO, challengeDTOCommonResponse.getData());
    }

    @Test
    @DisplayName("실패한 돈길 삭제 요청 시, 정상 response 테스트")
    public void deleteChallengeIfFailedChallengeSuccessTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(failed)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            newChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(5)),
            Timestamp.class
        );

        kid.setDeleteChallenge(Timestamp.valueOf(LocalDateTime.now()));

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(newChallenge)
            .member("parent").build();

        Progress progress = Progress.builder().challenge(newChallenge).weeks(1L).isAchieved(false)
            .build();

        Progress progress1 = Progress.builder().challenge(newChallenge).weeks(2L).isAchieved(false)
            .build();

        Progress progress2 = Progress.builder().challenge(newChallenge).weeks(3L).isAchieved(false)
            .build();

        Progress progress3 = Progress.builder().challenge(newChallenge).weeks(4L).isAchieved(false)
            .build();

        List<Progress> progressList = List.of(progress, progress1, progress2, progress3);

        newChallenge.setProgressList(progressList);

        //when
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(challengeUser));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.deleteChallenge(
            kidUser, newChallenge.getId());
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());
        Assertions.assertEquals(challengeDTO, challengeDTOCommonResponse.getData());
    }

    @Test
    @DisplayName("거절당한 돈길 삭제 요청 시, 정상 response 테스트")
    public void deleteChallengeIfRejectedChallengeSuccessTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(rejected)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            newChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        kid.setDeleteChallenge(Timestamp.valueOf(LocalDateTime.now()));

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(newChallenge)
            .member("parent").build();

        Comment comment = Comment.builder().challenge(newChallenge).user(parentUser).content("아쉽구나")
            .build();

        newChallenge.setComment(comment);

        //when
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(challengeUser));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.deleteChallenge(
            kidUser, newChallenge.getId());
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        ArgumentCaptor<Comment> ccCaptor = ArgumentCaptor.forClass(Comment.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());
        Mockito.verify(mockCommentRepository, Mockito.times(1)).delete(ccCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());
        Assertions.assertEquals(comment, ccCaptor.getValue());
        Assertions.assertEquals(challengeDTO, challengeDTOCommonResponse.getData());
    }

    @Test
    @DisplayName("제안중인 돈길 삭제 요청 시, 정상 response 테스트")
    public void deleteChallengeIfPendingChallengeSuccessTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            newChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(5)),
            Timestamp.class
        );

        kid.setDeleteChallenge(Timestamp.valueOf(LocalDateTime.now()));

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(newChallenge)
            .member("parent").build();

        //when
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(challengeUser));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.deleteChallenge(
            kidUser, newChallenge.getId());
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());
        Assertions.assertEquals(challengeDTO, challengeDTOCommonResponse.getData());
    }

    @Test
    @DisplayName("걷고 있는 돈길 삭제 요청 시, 삭제한지 2주가 경과되지 않았다면 403 에러 테스트")
    public void deleteChallengeIfWalkingChallengeAndNotYetTwoWeeksForbiddenTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            newChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(5)),
            Timestamp.class
        );

        kid.setDeleteChallenge(Timestamp.valueOf(LocalDateTime.now()));

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(newChallenge)
            .member("parent").build();

        Progress progress = Progress.builder().challenge(newChallenge).weeks(1L).isAchieved(false)
            .build();

        Progress progress1 = Progress.builder().challenge(newChallenge).weeks(2L).isAchieved(false)
            .build();

        Progress progress2 = Progress.builder().challenge(newChallenge).weeks(3L).isAchieved(false)
            .build();

        Progress progress3 = Progress.builder().challenge(newChallenge).weeks(4L).isAchieved(false)
            .build();

        List<Progress> progressList = List.of(progress, progress1, progress2, progress3);

        newChallenge.setProgressList(progressList);

        //when
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(challengeUser));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> challengeController.deleteChallenge(kidUser,
                newChallenge.getId()));
    }

    @Test
    @DisplayName("돈길 삭제 요청 시, 돈길을 만든 유저가 아니라면 403 에러 테스트")
    public void deleteChallengeIfNotMatchChallengeUserForbiddenTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        User user = User.builder().id(2L)
            .username("user2")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid2 = Kid.builder().id(2L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(user)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(failed)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            newChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(5)),
            Timestamp.class
        );

        kid.setDeleteChallenge(Timestamp.valueOf(LocalDateTime.now().minusWeeks(3L)));

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(newChallenge)
            .member("parent").build();

        Progress progress = Progress.builder().challenge(newChallenge).weeks(1L).isAchieved(false)
            .build();

        Progress progress1 = Progress.builder().challenge(newChallenge).weeks(2L).isAchieved(false)
            .build();

        Progress progress2 = Progress.builder().challenge(newChallenge).weeks(3L).isAchieved(false)
            .build();

        Progress progress3 = Progress.builder().challenge(newChallenge).weeks(4L).isAchieved(false)
            .build();

        List<Progress> progressList = List.of(progress, progress1, progress2, progress3);

        newChallenge.setProgressList(progressList);

        //when
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(challengeUser));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> challengeController.deleteChallenge(user,
                newChallenge.getId()));
    }

    // Todo: 돈길 리스트 가져오기 API 테스트 코드 작성
    @Test
    @DisplayName("제안중인 돈길 리스트 가져오기 요청 시, 정상 response 테스트")
    public void getPendingChallengeListSuccessTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        //parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge2 = Challenge.builder().id(3L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge3 = Challenge.builder().id(4L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge rejectedChallenge = Challenge.builder().id(5L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(rejected)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Comment comment = Comment.builder().user(parentUser).challenge(rejectedChallenge)
            .content("아쉽구나").build();

        rejectedChallenge.setComment(comment);

        List<Challenge> challengeList = List.of(newChallenge, newChallenge1, newChallenge2,
            newChallenge3, rejectedChallenge);

        List<ChallengeUser> challengeUserList = challengeList.stream().map(
            challenge -> ChallengeUser.builder().challenge(challenge).user(kidUser).member("parent")
                .build()).collect(Collectors.toList());

        List<ChallengeDTO> challengeDTOList = challengeList.stream()
            .map(challenge -> new ChallengeDTO(challenge, null, challenge.getComment())).collect(
                Collectors.toList());

        //when
        Mockito.when(mockChallengeUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(challengeUserList);

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        CommonResponse<List<ChallengeDTO>> challengeControllerListChallenge = challengeController.getListChallenge(
            kidUser,
            "pending");
        Assertions.assertEquals(challengeDTOList, challengeControllerListChallenge.getData());
    }

    @Test
    @DisplayName("걷고 있는 돈길 리스트 가져오기 중 성공한 돈길 및 실패한 돈길 업데이트 테스트")
    public void getWalkingChallengeIfChallengeStatusAchievedOrFailedUpdateTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        //parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge walkingChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            walkingChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        Progress walkingProgress = Progress.builder().challenge(walkingChallenge).weeks(1L)
            .isAchieved(true)
            .build();

        Progress walkingProgress1 = Progress.builder().challenge(walkingChallenge).weeks(2L)
            .isAchieved(false)
            .build();

        List<Progress> walkingProgressList = List.of(walkingProgress, walkingProgress1);

        ReflectionTestUtils.setField(
            walkingProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        walkingChallenge.setProgressList(walkingProgressList);

        Challenge achievedChallenge = Challenge.builder().id(2L).title("성공한 돈길")
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(4L)
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            achievedChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(7)),
            Timestamp.class
        );

        Progress achievedProgress = Progress.builder().challenge(achievedChallenge).weeks(1L)
            .isAchieved(true)
            .build();

        Progress achievedProgress1 = Progress.builder().challenge(achievedChallenge).weeks(2L)
            .isAchieved(true).build();

        Progress achievedProgress2 = Progress.builder().challenge(achievedChallenge).weeks(3L)
            .isAchieved(true).build();

        Progress achievedProgress3 = Progress.builder().challenge(achievedChallenge).weeks(4L)
            .isAchieved(false).build();

        List<Progress> achievedProgressList = List.of(achievedProgress, achievedProgress1,
            achievedProgress2, achievedProgress3);

        ReflectionTestUtils.setField(
            achievedProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(7)),
            Timestamp.class
        );

        achievedChallenge.setProgressList(achievedProgressList);

        Challenge failedChallenge = Challenge.builder().id(3L).title("실패한 돈길")
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            failedChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(3)),
            Timestamp.class
        );

        Progress failedProgress = Progress.builder().challenge(failedChallenge).weeks(1L)
            .isAchieved(false)
            .build();

        Progress failedProgress1 = Progress.builder().challenge(failedChallenge).weeks(2L)
            .isAchieved(false)
            .build();

        Progress failedProgress2 = Progress.builder().challenge(failedChallenge).weeks(3L)
            .isAchieved(false)
            .build();

        List<Progress> failedProgressList = List.of(failedProgress, failedProgress1,
            failedProgress2);

        ReflectionTestUtils.setField(
            failedProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(3)),
            Timestamp.class
        );

        failedChallenge.setProgressList(failedProgressList);

        List<Challenge> challengeList = List.of(walkingChallenge, achievedChallenge,
            failedChallenge);

        List<ChallengeUser> challengeUserList = challengeList.stream().map(
            challenge -> {
                ChallengeUser challengeUser = ChallengeUser.builder().challenge(challenge)
                    .user(kidUser)
                    .member("parent")
                    .build();
                challenge.setChallengeUser(challengeUser);
                return challengeUser;
            }).collect(Collectors.toList());

//        List<ChallengeDTO> challengeDTOList = challengeList.stream()
//            .map(challenge -> {
//                List<ProgressDTO> progressDTOList = challenge.getProgressList().stream()
//                    .map(progress -> new ProgressDTO(progress, challenge)).collect(
//                        Collectors.toList());
//                return new ChallengeDTO(challenge, progressDTOList, null);
//            }).collect(
//                Collectors.toList());

        List<ProgressDTO> progressDTOList = walkingChallenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, walkingChallenge)).collect(
                Collectors.toList());

        List<ProgressDTO> failedProgressDTOList = failedChallenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, failedChallenge)).collect(
                Collectors.toList());

        //when
        Mockito.when(mockChallengeUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(challengeUserList);

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        failedChallenge.setChallengeStatus(walking);
        CommonResponse<List<ChallengeDTO>> challengeControllerListChallenge = challengeController.getListChallenge(
            kidUser,
            "walking");
        // 걷고 있는 돈길만 가져오는지
        failedChallenge.setChallengeStatus(failed);
        ChallengeDTO challengeDTO = new ChallengeDTO(walkingChallenge, progressDTOList, null);

        ChallengeDTO failedChallengeDTO = new ChallengeDTO(failedChallenge, failedProgressDTOList,
            null);

        List<ChallengeDTO> challengeDTOList = List.of(challengeDTO, failedChallengeDTO);
        Assertions.assertEquals(challengeDTOList, challengeControllerListChallenge.getData());

        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        ArgumentCaptor<Notification> nCaptor = ArgumentCaptor.forClass(Notification.class);

        Mockito.verify(mockChallengeRepository, Mockito.times(2)).save(cCaptor.capture());
        // 돈길 성공 / 실패 / 자녀 레벨업 알림 검증
        Mockito.verify(mockNotificationRepository, Mockito.times(3)).save(nCaptor.capture());

        Assertions.assertAll(() -> {
            if (cCaptor.getValue().getChallengeStatus() == achieved) {
                Assertions.assertEquals(cCaptor.getValue().getTitle(), "성공한 돈길");
            } else {
                Assertions.assertEquals(cCaptor.getValue().getTitle(), "실패한 돈길");
            }
        });
    }

    @Test
    @DisplayName("돈길 리스트 가져오기 시, Query Param 에러 발생 시 BadRequest 에러 테스트")
    public void getChallengeListIfQueryParamErrorBadRequestErrorTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        //parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge walkingChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            walkingChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        Progress walkingProgress = Progress.builder().challenge(walkingChallenge).weeks(1L)
            .isAchieved(true)
            .build();

        Progress walkingProgress1 = Progress.builder().challenge(walkingChallenge).weeks(2L)
            .isAchieved(false)
            .build();

        List<Progress> walkingProgressList = List.of(walkingProgress, walkingProgress1);

        ReflectionTestUtils.setField(
            walkingProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        walkingChallenge.setProgressList(walkingProgressList);

        Challenge achievedChallenge = Challenge.builder().id(2L).title("성공한 돈길")
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(4L)
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            achievedChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(7)),
            Timestamp.class
        );

        Progress achievedProgress = Progress.builder().challenge(achievedChallenge).weeks(1L)
            .isAchieved(true)
            .build();

        Progress achievedProgress1 = Progress.builder().challenge(achievedChallenge).weeks(2L)
            .isAchieved(true).build();

        Progress achievedProgress2 = Progress.builder().challenge(achievedChallenge).weeks(3L)
            .isAchieved(true).build();

        Progress achievedProgress3 = Progress.builder().challenge(achievedChallenge).weeks(4L)
            .isAchieved(false).build();

        List<Progress> achievedProgressList = List.of(achievedProgress, achievedProgress1,
            achievedProgress2, achievedProgress3);

        ReflectionTestUtils.setField(
            achievedProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(7)),
            Timestamp.class
        );

        achievedChallenge.setProgressList(achievedProgressList);

        Challenge failedChallenge = Challenge.builder().id(3L).title("실패한 돈길")
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            failedChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(3)),
            Timestamp.class
        );

        Progress failedProgress = Progress.builder().challenge(failedChallenge).weeks(1L)
            .isAchieved(false)
            .build();

        Progress failedProgress1 = Progress.builder().challenge(failedChallenge).weeks(2L)
            .isAchieved(false)
            .build();

        Progress failedProgress2 = Progress.builder().challenge(failedChallenge).weeks(3L)
            .isAchieved(false)
            .build();

        List<Progress> failedProgressList = List.of(failedProgress, failedProgress1,
            failedProgress2);

        ReflectionTestUtils.setField(
            failedProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(3)),
            Timestamp.class
        );

        failedChallenge.setProgressList(failedProgressList);

        List<Challenge> challengeList = List.of(walkingChallenge, achievedChallenge,
            failedChallenge);

        List<ChallengeUser> challengeUserList = challengeList.stream().map(
            challenge -> {
                ChallengeUser challengeUser = ChallengeUser.builder().challenge(challenge)
                    .user(kidUser)
                    .member("parent")
                    .build();
                challenge.setChallengeUser(challengeUser);
                return challengeUser;
            }).collect(Collectors.toList());

//        List<ChallengeDTO> challengeDTOList = challengeList.stream()
//            .map(challenge -> {
//                List<ProgressDTO> progressDTOList = challenge.getProgressList().stream()
//                    .map(progress -> new ProgressDTO(progress, challenge)).collect(
//                        Collectors.toList());
//                return new ChallengeDTO(challenge, progressDTOList, null);
//            }).collect(
//                Collectors.toList());

        List<ProgressDTO> progressDTOList = walkingChallenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, walkingChallenge)).collect(
                Collectors.toList());

        List<ProgressDTO> failedProgressDTOList = failedChallenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, failedChallenge)).collect(
                Collectors.toList());

        //when
        Mockito.when(mockChallengeUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(challengeUserList);

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        Assertions.assertThrows(BadRequestException.class,
            () -> challengeController.getListChallenge(kidUser, "err"));
    }

    // Todo: 자녀의 돈길 리스트 가져오기
    @Test
    @DisplayName("자녀의 제안중인 돈길 리스트 가져오기 요청 시 정상 response 테스트")
    public void getKidPendingChallengeListSuccessTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        User kidUser1 = User.builder().id(4L)
            .username("kiduser1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        Kid kid1 = Kid.builder().id(2L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser1).build();

        kidUser.setKid(kid);
        kidUser1.setKid(kid1);

        //parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        User fatherUser = User.builder().id(3L)
            .username("user3")
            .isKid(false)
            .isFemale(false)
            .birthday("19760101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        Parent parent1 = Parent.builder().id(2L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(fatherUser)
            .build();

        parentUser.setParent(parent);

        fatherUser.setParent(parent1);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();
        FamilyUser familyUser2 = FamilyUser.builder().id(3L).family(family).user(fatherUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser, familyUser1, familyUser2);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge2 = Challenge.builder().id(3L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge3 = Challenge.builder().id(4L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge rejectedChallenge = Challenge.builder().id(5L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(rejected)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Comment comment = Comment.builder().user(parentUser).challenge(rejectedChallenge)
            .content("아쉽구나").build();

        rejectedChallenge.setComment(comment);

        List<Challenge> challengeList = List.of(newChallenge, newChallenge1, newChallenge2,
            newChallenge3);

        List<ChallengeUser> challengeUserList = challengeList.stream().map(
            challenge -> ChallengeUser.builder().challenge(challenge).user(kidUser).member("parent")
                .build()).collect(Collectors.toList());

        List<ChallengeDTO> challengeDTOList = challengeList.stream()
            .map(challenge -> new ChallengeDTO(challenge, null, challenge.getComment())).collect(
                Collectors.toList());

        //when
        Mockito.when(mockChallengeUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockKidRepository.findById(kid.getId())).thenReturn(Optional.of(kid));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        CommonResponse<KidChallengeListDTO> challengeControllerListKidChallenge = challengeController.getListKidChallenge(
            parentUser, kid.getId(), "pending");
        KidChallengeListDTO kidChallengeListDTO = new KidChallengeListDTO(kidUser,
            challengeDTOList);
        Assertions.assertEquals(kidChallengeListDTO, challengeControllerListKidChallenge.getData());
    }

    @Test
    @DisplayName("자녀의 걷고 있는 돈길 리스트 가져오기 요청 시, 정상 response 테스트")
    public void getKidWalkingChallengeListSuccessTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        //parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge walkingChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            walkingChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        Progress walkingProgress = Progress.builder().challenge(walkingChallenge).weeks(1L)
            .isAchieved(true)
            .build();

        Progress walkingProgress1 = Progress.builder().challenge(walkingChallenge).weeks(2L)
            .isAchieved(false)
            .build();

        List<Progress> walkingProgressList = List.of(walkingProgress, walkingProgress1);

        ReflectionTestUtils.setField(
            walkingProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        walkingChallenge.setProgressList(walkingProgressList);

        Challenge achievedChallenge = Challenge.builder().id(2L).title("성공한 돈길")
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(4L)
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            achievedChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(7)),
            Timestamp.class
        );

        Progress achievedProgress = Progress.builder().challenge(achievedChallenge).weeks(1L)
            .isAchieved(true)
            .build();

        Progress achievedProgress1 = Progress.builder().challenge(achievedChallenge).weeks(2L)
            .isAchieved(true).build();

        Progress achievedProgress2 = Progress.builder().challenge(achievedChallenge).weeks(3L)
            .isAchieved(true).build();

        Progress achievedProgress3 = Progress.builder().challenge(achievedChallenge).weeks(4L)
            .isAchieved(false).build();

        List<Progress> achievedProgressList = List.of(achievedProgress, achievedProgress1,
            achievedProgress2, achievedProgress3);

        ReflectionTestUtils.setField(
            achievedProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(7)),
            Timestamp.class
        );

        achievedChallenge.setProgressList(achievedProgressList);

        Challenge failedChallenge = Challenge.builder().id(3L).title("실패한 돈길")
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ReflectionTestUtils.setField(
            failedChallenge,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(3)),
            Timestamp.class
        );

        Progress failedProgress = Progress.builder().challenge(failedChallenge).weeks(1L)
            .isAchieved(false)
            .build();

        Progress failedProgress1 = Progress.builder().challenge(failedChallenge).weeks(2L)
            .isAchieved(false)
            .build();

        Progress failedProgress2 = Progress.builder().challenge(failedChallenge).weeks(3L)
            .isAchieved(false)
            .build();

        List<Progress> failedProgressList = List.of(failedProgress, failedProgress1,
            failedProgress2);

        ReflectionTestUtils.setField(
            failedProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(3)),
            Timestamp.class
        );

        failedChallenge.setProgressList(failedProgressList);

        List<Challenge> challengeList = List.of(walkingChallenge, achievedChallenge,
            failedChallenge);

        List<ChallengeUser> challengeUserList = challengeList.stream().map(
            challenge -> {
                ChallengeUser challengeUser = ChallengeUser.builder().challenge(challenge)
                    .user(kidUser)
                    .member("parent")
                    .build();
                challenge.setChallengeUser(challengeUser);
                return challengeUser;
            }).collect(Collectors.toList());

        List<ProgressDTO> progressDTOList = walkingChallenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, walkingChallenge)).collect(
                Collectors.toList());

        List<ProgressDTO> failedProgressDTOList = failedChallenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, failedChallenge)).collect(
                Collectors.toList());

        //when
        Mockito.when(mockChallengeUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockKidRepository.findById(kid.getId())).thenReturn(Optional.of(kid));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        failedChallenge.setChallengeStatus(walking);
        CommonResponse<KidChallengeListDTO> challengeControllerListKidChallenge = challengeController.getListKidChallenge(
            parentUser,
            kid.getId(),
            "walking");
        // 걷고 있는 돈길만 가져오는지
        failedChallenge.setChallengeStatus(failed);
        ChallengeDTO challengeDTO = new ChallengeDTO(walkingChallenge, progressDTOList, null);

        ChallengeDTO failedChallengeDTO = new ChallengeDTO(failedChallenge, failedProgressDTOList,
            null);

        List<ChallengeDTO> challengeDTOList = List.of(challengeDTO, failedChallengeDTO);
        KidChallengeListDTO kidChallengeListDTO = new KidChallengeListDTO(kid.getId(),
            challengeDTOList);
        Assertions.assertEquals(kidChallengeListDTO, challengeControllerListKidChallenge.getData());

        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        ArgumentCaptor<Notification> nCaptor = ArgumentCaptor.forClass(Notification.class);

        Mockito.verify(mockChallengeRepository, Mockito.times(2)).save(cCaptor.capture());
        // 돈길 성공 / 실패 / 자녀 레벨업 알림 검증
        Mockito.verify(mockNotificationRepository, Mockito.times(3)).save(nCaptor.capture());

        Assertions.assertAll(() -> {
            if (cCaptor.getValue().getChallengeStatus() == achieved) {
                Assertions.assertEquals(cCaptor.getValue().getTitle(), "성공한 돈길");
            } else {
                Assertions.assertEquals(cCaptor.getValue().getTitle(), "실패한 돈길");
            }
        });
    }

    @Test
    @DisplayName("자녀의 제안중인 돈길 리스트 가져오기 요청 시, 해당 돈길의 계약 부모가 아니면 가져오지 않는지 테스트")
    public void getKidPendingChallengeListOnlyMatchContractUserResponseDataTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        User kidUser1 = User.builder().id(4L)
            .username("kiduser1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        Kid kid1 = Kid.builder().id(2L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser1).build();

        kidUser.setKid(kid);
        kidUser1.setKid(kid1);

        //parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        User fatherUser = User.builder().id(3L)
            .username("user3")
            .isKid(false)
            .isFemale(false)
            .birthday("19760101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        Parent parent1 = Parent.builder().id(2L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(fatherUser)
            .build();

        parentUser.setParent(parent);

        fatherUser.setParent(parent1);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();
        FamilyUser familyUser2 = FamilyUser.builder().id(3L).family(family).user(fatherUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser, familyUser1, familyUser2);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge2 = Challenge.builder().id(3L).title(challengeRequest.getTitle())
            .contractUser(fatherUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge3 = Challenge.builder().id(4L).title(challengeRequest.getTitle())
            .contractUser(fatherUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge rejectedChallenge = Challenge.builder().id(5L).title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(rejected)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Comment comment = Comment.builder().user(parentUser).challenge(rejectedChallenge)
            .content("아쉽구나").build();

        rejectedChallenge.setComment(comment);

        List<Challenge> challengeList = List.of(newChallenge, newChallenge1);

        List<ChallengeUser> challengeUserList = challengeList.stream().map(
            challenge -> ChallengeUser.builder().challenge(challenge).user(kidUser).member("parent")
                .build()).collect(Collectors.toList());

        List<ChallengeDTO> challengeDTOList = challengeList.stream()
            .map(challenge -> new ChallengeDTO(challenge, null, challenge.getComment())).collect(
                Collectors.toList());

        //when
        Mockito.when(mockChallengeUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockKidRepository.findById(kid.getId())).thenReturn(Optional.of(kid));

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        CommonResponse<KidChallengeListDTO> challengeControllerListKidChallenge = challengeController.getListKidChallenge(
            parentUser, kid.getId(), "pending");
        KidChallengeListDTO kidChallengeListDTO = new KidChallengeListDTO(kidUser,
            challengeDTOList);
        Assertions.assertEquals(kidChallengeListDTO, challengeControllerListKidChallenge.getData());
    }


    @Test
    @DisplayName("자녀의 돈길 리스트 가져오기 시, Query Param 에러 발생 시 BadRequest 에러 테스트")
    public void getKidChallengeListIfQueryParamErrorBadRequestErrorTest() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        //parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

        //when

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, notificationService);
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        Assertions.assertThrows(BadRequestException.class,
            () -> challengeController.getListKidChallenge(parentUser, kid.getId(), "err"));
    }
}
