package com.code.touragentbot.configs.dev;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class RabbitMQConfig {

    public static final String QUEUE = "session_queue";
    public static final String STOP_QUEUE = "stop_queue";
    public static final String OFFER_QUEUE = "offer_queue";
    public static final String ACCEPTED_QUEUE = "accepted_queue";
    public static final String EXCHANGE = "session_exchange";
    public static final String ROUTING_KEY = "session_routing_key";


    @Bean
    public Queue queue(){
        return new Queue(QUEUE);
    }

    @Bean
    public Queue stopQueue(){ return new Queue(STOP_QUEUE);}

    @Bean
    public Queue offerQueue(){ return new Queue(OFFER_QUEUE);}

    @Bean
    public Queue acceptedQueue(){ return new Queue(ACCEPTED_QUEUE); }

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory factory){
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
