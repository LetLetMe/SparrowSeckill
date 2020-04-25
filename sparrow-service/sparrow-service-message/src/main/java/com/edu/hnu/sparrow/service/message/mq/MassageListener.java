package com.edu.hnu.sparrow.service.message.mq;



import com.edu.hnu.sparrow.service.message.config.MassageProperties;

import com.edu.hnu.sparrow.service.message.utils.MassageUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@EnableConfigurationProperties(MassageProperties.class)
public class MassageListener {
    @Autowired
    private MassageUtil smsUtils;

    @Autowired
    private MassageProperties prop;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mymall.sms.queue", durable = "true"),
            exchange = @Exchange(value = "mymall.sms.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}))
    public void listenSms(Map<String, String> msg) throws Exception {
        if (msg == null || msg.size() <= 0) {
            // 放弃处理
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");

        if (StringUtils.isBlank(phone) || StringUtils.isBlank(code)) {
            // 放弃处理
            return;
        }
//        // 发送消息
//        SendSmsResponse resp = this.smsUtils.sendSms(phone, code,
//                prop.getSignName(),
//                prop.getVerifyCodeTemplate());

        // 发送失败
//        throw new RuntimeException(); 这句为什么会循环使用啊
        System.out.println("消费成功"+phone+" "+code);
    }
}
