package com.energymanagement.devicemanagement.service;

import com.energymanagement.devicemanagement.model.UserCopy;
import com.energymanagement.devicemanagement.repository.UserCopyRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserSyncConsumer
{

    private final UserCopyRepository userCopyRepository;

    public UserSyncConsumer(UserCopyRepository userCopyRepository)
    {
        this.userCopyRepository = userCopyRepository;
    }

    @RabbitListener(
            queues = "sync-queue-device",
            containerFactory = "syncRabbitListenerContainerFactory"
    )
    public void consumeSyncEvent(Map<String, Object> message)
    {
        try {
            String eventType = (String) message.get("eventType");

            System.out.println("Received sync event in Device Service: " + message);

            // Proceseaza event USER_CREATED
            if ("USER_CREATED".equals(eventType))
            {
                Long userId = ((Number) message.get("userId")).longValue();

                if (!userCopyRepository.existsById(userId))
                {
                    UserCopy userCopy = new UserCopy(userId);
                    userCopyRepository.save(userCopy);
                    System.out.println("Added user to users_copy: userId=" + userId);

                }
                else
                {
                    System.out.println("User already exists in users_copy: userId=" + userId);
                }
            }
            else if ("USER_DELETED".equals(eventType))
            {
                Long userId = ((Number) message.get("userId")).longValue();

                // Sterge user din users_copy
                if (userCopyRepository.existsById(userId))
                {
                    userCopyRepository.deleteById(userId);
                    System.out.println("Deleted user from users_copy: userId=" + userId);

                }
                else
                {
                    System.out.println("User not found in users_copy: userId=" + userId);
                }
            }


        } catch (Exception e) {

            System.err.println("Error processing sync event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}