package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.controller.request.NoticeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.NoticeServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final ExpoNotificationServiceImpl expoNotificationService;
    private final NoticeServiceImpl noticeService;
    private final UserRepository userRepository;

    @ApiOperation(value = "모든 유저에게 알림")
    @PostMapping(value = "/all_user", produces = "application/json; charset=utf-8")
    public CommonResponse<String> allSendNotification(
        @RequestBody NoticeRequest allSendNotificationRequest,
        @AuthenticationPrincipal User authUser) {

//        if (authUser.getId() != 9L) {
//            throw new ForbiddenException(ErrorCode.ALL_NOTIFICATION_AUTH_ERROR.getErrorCode());
//        }
        String title = allSendNotificationRequest.getTitle();
        String body = allSendNotificationRequest.getBody();
        String message = allSendNotificationRequest.getMessage();
        noticeService.postNotice(title, body);
        userRepository.findAll().stream()
            .filter(user -> user.getExpoToken() != null && !Objects.equals(user.getExpoToken(),
                "web"))
            .forEach(user -> {
                expoNotificationService.sendMessage(user, title, message, null);
            });
        return CommonResponse.onSuccess("NOTIFICATION SUCCESS");
    }

    @Async
    @ApiOperation(value = "돈길 상태 변경 알림")
    public void notification(Challenge challenge, User authUser) {

        String title = challenge.getChallengeStatus() == ChallengeStatus.WALKING ?
            challenge.getContractUser().getUsername() + "님이 제안한 돈길을 수락했어요\uD83D\uDE46\u200D"
            : challenge.getContractUser().getUsername() + "님이 제안한 돈길을 거절했어요\uD83D\uDE45\u200D";
        String notificationBody =
            challenge.getChallengeStatus() == ChallengeStatus.WALKING
                ? "수락한 돈길 빨리 걸으러 가요\uD83E\uDD38"
                : "그 이유가 무엇인지 알아보러 가요\uD83D\uDE25";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("challengeId", challenge.getId());
        newMap.put("userId", authUser.getId());
        expoNotificationService.sendMessage(authUser, title, notificationBody, newMap);
        log.info("유저 {}의 돈길 {}의 {} 상태변경 알림", authUser.getId(), challenge.getId(),
            challenge.getChallengeStatus());
    }

    @Async
    @ApiOperation(value = "유저 레벨업 직전 알림")
    public void userLevelUpMinusOne(User authUser) {

        String title = "레벨업까지 딱 한개만!";
        String notificationBody = "레벨업하기까지 \uD83D\uDD381 개\uD83D\uDD38의 돈길만 완주하면 돼요";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("userId", authUser.getId());
        expoNotificationService.sendMessage(authUser, title, notificationBody, newMap);
        log.info("유저 id = {}의 레벨업 직전 알림", authUser.getId());
    }

    @Async
    @ApiOperation(value = "유저 레벨업 절반 달성 알림")
    public void userLevelUpHalf(User authUser) {

        String title = "벌써 절반이나 왔네요\uD83D\uDCAF";
        String notificationBody = "레벨업까지 절반 남았어요.힘내세요\uD83D\uDC97";

        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("userId", authUser.getId());
        expoNotificationService.sendMessage(authUser, title, notificationBody, newMap);
        log.info("유저 id = {}의 레벨업 절반 달성 알림", authUser.getId());
    }

    @Async
    @ApiOperation(value = "자녀가 돈길 제안했을 때 부모 알림")
    public void createPendingChallengeNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "\uD83D\uDD14 " + challengeUser.getUser().getUsername() + "님이 돈길을 제안했어요";
        String notificationBody =
            challengeUser.getUser().getUsername() + "님이 돈길을 제안했어요! 수락하러 가볼까요?";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        expoNotificationService.sendMessage(contractUser, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {} 돈길 id = {} 의 돈길 제안", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    @ApiOperation(value = "자녀가 돈길을 걸었을 때 부모 알림")
    public void runProgressNotification(User contractUser, ChallengeUser challengeUser) {

        String title = challengeUser.getUser().getUsername() + "님이 돈길을 걸었어요! \uD83C\uDFC3\u200D";
        String notificationBody =
            challengeUser.getUser().getUsername() + "님이 어떤 돈길을 걸었을까요?\n확인하러가요❤️\u200D";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        expoNotificationService.sendMessage(contractUser, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {}의 돈길 id = {} 돈길 걷기 알림 전송", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    @ApiOperation(value = "돈길을 완주했을 때 부모 알림")
    public void achieveChallengeNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "\uD83D\uDEA8자녀가 돈길을 완주했어요";
        String notificationBody =
            "실제로 다 모았는지 확인하시고\n그동안 고생한" + challengeUser.getUser().getUsername()
                + "님에게 이자\uD83D\uDCB0로 보상해주세요!";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        expoNotificationService.sendMessage(contractUser, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {}의 돈길 id = {} 돈길 완주 알림 전송", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    @ApiOperation(value = "자녀 레벨업 시 부모 알림")
    public void kidLevelUpNotification(User contractUser, User user, Long level, Long afterLevel) {

        String title = "자녀가 레벨업을 했어요💯";
        String notificationBody =
            user.getUsername() + "님이 레벨" + level + "에서 레벨" + afterLevel + "로 올랐어요! 확인해볼까요?";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", user.getId());
        expoNotificationService.sendMessage(contractUser, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {}의 레벨업 알림 전송", contractUser.getId(), user.getId());
    }

    @Async
    @ApiOperation(value = "돈길 실패 시 부모 알림")
    public void challengeFailedNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "\uD83D\uDEA8자녀가 돈길을 실패했어요";
        String notificationBody = "실패한 돈길을 확인한 후, 자녀에게 격려해주세요\uD83C\uDD98\u2028 \n실패한 돈길들은 마이페이지 - 돈길 기록에서 확인가능해요";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        expoNotificationService.sendMessage(contractUser, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {}의 돈길 id = {} 돈길 실패 알림 전송", contractUser.getId(),
            challengeUser.getChallenge().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    public void newFamilyUserNotification(User newFamilyUser, List<FamilyUser> familyUserList) {

        String title = "가족그룹\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66에 새로 참여했어요";
        String notificationBody = "누가 가족그룹에 참여했는지 확인해요!\uD83D\uDCAB";
        HashMap<String, Object> newMap = new HashMap<>();
//        newMap.put("user", newFamilyUser.getId());
        familyUserList.forEach(familyUser -> {
            User user = familyUser.getUser();
            expoNotificationService.sendMessage(user, title, notificationBody, newMap);
            log.info("기존 가족 구성원 id = {}에게 유저 id = {}의 가족 참여 알림 전송", familyUser.getUser().getId(),
                newFamilyUser.getId());
        });
    }
}
