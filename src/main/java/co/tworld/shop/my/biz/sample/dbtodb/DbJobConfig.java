package co.tworld.shop.my.biz.sample.dbtodb;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Configuration
public class DbJobConfig {
    private MemberRepository memberRepository;

    @Bean
    public Job unPaidMemberJob(
            JobBuilderFactory jobBuilderFactory,
            Step unPaidMemberJobStep
    ) {
        return jobBuilderFactory.get("unPaidMemberJob")
                .preventRestart()
                .start(unPaidMemberJobStep)
                .build();
    }

    @Bean
    public Step unPaidMemberJobStep(
            StepBuilderFactory stepBuilderFactory
    ) {
        return stepBuilderFactory.get("unPaidMemberJobStep")
                .<Member, Member> chunk(10)
                .reader(unPaidMemberReader())
                .processor(this.unPaidMemberProcessor())
                .writer(this.unPaidMemberWriter())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<Member> unPaidMemberReader() {
        List<Member> activeMembers = memberRepository.findByStatusEquals(MemberStatus.ACTIVE);
        List<Member> unPaidMembers = new ArrayList<>();
        for (Member member : activeMembers) {
            if(member.isUnpaid()) {
                unPaidMembers.add(member);
            }
        }
        return new ListItemReader<>(unPaidMembers);
    }

    public ItemProcessor<Member, Member> unPaidMemberProcessor() {
//        return Member::setStatusByunPaid;
        return new ItemProcessor<Member, Member>() {
            @Override
            public Member process(Member member) throws Exception {
                return member.setStatusByUnPaid();
            }
        };
    }

    public ItemWriter<Member> unPaidMemberWriter() {
        return ((List<? extends Member> memberList) ->
                memberRepository.saveAll(memberList));
    }
}
