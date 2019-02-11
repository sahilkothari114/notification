package com.contest.notification.consumer;


import com.contest.notification.dto.Header;
import com.contest.notification.dto.Share;
import com.contest.notification.dto.SubscriptionNotice;
import com.contest.notification.entity.NotificationData;
import com.contest.notification.entity.Template;
import com.contest.notification.entity.User;
import com.contest.notification.exception.FieldsCanNotBeEmpty;
import com.contest.notification.notificationEnum.NotificationMedium;
import com.contest.notification.notificationMedium.Sender;
import com.contest.notification.notificationMedium.SenderFactory;
import com.contest.notification.service.NotificationService;
import com.contest.notification.service.TemplateService;
import com.contest.notification.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubscriptionNoticeConsumer implements Consumer{

    @Autowired
    TemplateService templateService;

    @Autowired
    UserService userService;

    @Autowired
    SenderFactory senderFactory;

    @Autowired
    NotificationService notificationService;

    @KafkaListener(topics="${subscriptionNotice.kafka.topic}",containerFactory = "HeaderKafkaListenerContainerFactory")
    public void receiveMessage(Header header) throws Exception {
        LOGGER.info("Received:"+ header);

        if(header == null)
            throw new FieldsCanNotBeEmpty("Header Cannot Be Empty");

        if(header.getReceiver() == null || header.getNotificationMedium() == null || header.getNotificationType() == null ||
                header.getNotificationTypeBody() == null || header.getTimeStamp() == null)
            throw new FieldsCanNotBeEmpty("Header Fields Cannot Be Empty");

        SubscriptionNotice subscriptionNotice = (SubscriptionNotice) header.getNotificationTypeBody();

        if(subscriptionNotice.getContestId() == null || subscriptionNotice.getContestName() == null ||
                subscriptionNotice.getFollowerIds().size() == 0) {
            throw new FieldsCanNotBeEmpty("Notification Body Fields Cannot Be Empty");
        }
        String message = processMessage(header);
        for (String userId: subscriptionNotice.getFollowerIds()) {
            User user= userService.findOne(userId);
            for (NotificationMedium medium: header.getNotificationMedium()) {
                Sender sender = senderFactory.getInstance(medium);
                sender.send(header,message,"Contest Subscription",user);
            }
        }

        NotificationData notificationData = new NotificationData();
        BeanUtils.copyProperties(header,notificationData);
        notificationData.setNotificationTypeBody(header.getNotificationTypeBody());
        notificationService.addNotification(notificationData);
    }

    @Override
    public String processMessage(Header header) throws Exception {

        Template template = templateService.findByTemplateName(header.getNotificationType().getValue());

        String str = template.getTemplate();
        int endIndex = 0;

        List<String> replacementArray = new ArrayList<>();
        SubscriptionNotice subscriptionNotice = (SubscriptionNotice)header.getNotificationTypeBody();
        User user = userService.findOne(header.getReceiver());
        replacementArray.add(user.getUserName());
        replacementArray.add(subscriptionNotice.getContestName());

        int i=0;
        LOGGER.info("Template : {}" , str);
        while(true) {

            int startIndex = str.indexOf("<",endIndex);
            if(startIndex == -1)
                break;
            endIndex = str.indexOf(">",endIndex);
            String replaceString = str.substring(startIndex, ++endIndex);
            if(replacementArray.size() > i) {
                str = str.replace(replaceString, replacementArray.get(i++));
            }

        }
        LOGGER.info("Final String : {}" , str);
        return str;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionNoticeConsumer.class);

}
