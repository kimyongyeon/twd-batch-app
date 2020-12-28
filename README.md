# 원본소스 주소 
```
git clone https://github.com/kimyongyeon/twd-batch-app.git
```

# controller 호출
http://localhost:10012/sample/jobLauncher/simpleJob?requestDate=20190101

# csv to db 템플릿 
```

// CSV 파일 읽어서 모델에 저장하는 메서드 
@Bean
public FlatFileItemReader<Person> reader() {
    return new FlatFileItemReaderBuilder<Person>()
            .name("personItemReader")
            .resource(new ClassPathResource("sample-data.csv"))
            .delimited()
            .names(new String[]{"firstName", "lastName"})
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }})
            .build();
}

// 저장된 모델을 편집하는 메서드 
@Bean
public ItemProcessor<Person, Person> processor() {
//        return new PersonItemProcessor();
    return new ItemProcessor<Person, Person>() {
        @Override
        public Person process(final Person person) throws Exception {
            log.info("********** This is unPaidMemberProcessor");
            final String firstName = person.getFirstName().toUpperCase();
            final String lastName = person.getLastName().toUpperCase();
            final Person transformedPerson = new Person(firstName, lastName);
            log.info("Converting (" + person + ") into (" + transformedPerson + ")");
            return transformedPerson;
        }
    };
}

// DB에 Insert하는 메서드 
@Bean
public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Person>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
            .dataSource(dataSource)
            .build();
}
``` 

# db to db 탬플릿
```

// DB를 select하여 모델에 넣는 메서드 
@Bean
@StepScope
public ListItemReader<Member> unPaidMemberReader() {
    log.info("********** This is unPaidMemberReader");
    List<Member> activeMembers = memberRepository.findByStatusEquals(MemberStatus.ACTIVE);
    log.info("          - activeMember SIZE : " + activeMembers.size());
    List<Member> unPaidMembers = new ArrayList<>();
    for (Member member : activeMembers) {
        if(member.isUnpaid()) {
            unPaidMembers.add(member);
        }
    }
    log.info("          - unPaidMember SIZE : " + unPaidMembers.size());
    return new ListItemReader<>(unPaidMembers);
}

// 모델의 값을 편집하는 메서드 
public ItemProcessor<Member, Member> unPaidMemberProcessor() {
//        return Member::setStatusByunPaid;
    return new ItemProcessor<Member, Member>() {
        @Override
        public Member process(Member member) throws Exception {
            log.info("********** This is unPaidMemberProcessor");
            return member.setStatusByUnPaid();
        }
    };
}

// DB 저장하는 메서드 
public ItemWriter<Member> unPaidMemberWriter() {
    log.info("********** This is unPaidMemberWriter");
    return ((List<? extends Member> memberList) ->
            memberRepository.saveAll(memberList));
}
```

# 권장 툴
- intellij: https://www.jetbrains.com/ko-kr/idea/
- STS: https://spring.io/tools
- visual studio code: https://code.visualstudio.com/download

# 도움되는 사이트 
- json editor: https://jsoneditoronline.org/#left=local.soyiro&right=local.fawige
- thymeleaf: https://www.thymeleaf.org/
- 변수명이름짓기: https://www.curioustore.com/#!/


# 프로젝트 최초 구성시
## 개발환경
- https://adoptopenjdk.net/?variant=openjdk15&jvmVariant=hotspot
- JDK: openJDK8
- JVM: OpenJ9
- lombok
 
## banner.txt 제목 변경
`아래 사이트를 이동후 제목을 만들어 넣어주세면 됩니다.`
http://patorjk.com/software/taag/#p=display&f=Calvin%20S&t=TWD%20BFF%20PROJECT

```   
${Ansi.YELLOW}***********************************************************************
${Ansi.GREEN} ╔╦╗╦ ╦╔╦╗  ╔╗ ╔═╗╔═╗  ╔═╗╦═╗╔═╗ ╦╔═╗╔═╗╔╦╗
${Ansi.GREEN}  ║ ║║║ ║║  ╠╩╗╠╣ ╠╣   ╠═╝╠╦╝║ ║ ║║╣ ║   ║
${Ansi.GREEN}  ╩ ╚╩╝═╩╝  ╚═╝╚  ╚    ╩  ╩╚═╚═╝╚╝╚═╝╚═╝ ╩
${Ansi.GREEN}Application                    : ${application.name}
${Ansi.GREEN}Port                           : ${server.port}
${Ansi.GREEN}Active                         : ${spring.profiles.active}
${Ansi.GREEN}Application Version            : ${application.formatted-version}
${Ansi.GREEN}Application Title              : ${application.title}
${Ansi.GREEN}Spring Boot Version            : ${spring-boot.version}
${Ansi.GREEN}Spring Boot Formatted Version  : ${spring-boot.formatted-version}
${Ansi.YELLOW}***********************************************************************
```

# 샘플코드 위치 
- controller: src\main\java\co\tworld\shop\my\biz\sample\controller\JobLauncherController.java
- csv to DB: src\main\java\co\tworld\shop\my\biz\sample\csvtodb
- DB to DB: src\main\java\co\tworld\shop\my\biz\sample\dbtodb
- Simple: src\main\java\co\tworld\shop\my\biz\sample\simple

# 주석방법
```
1. 메서드 
/**
 * @title makingName
 * @param mdn
 * @param prodNo
 * @param sampleVO
 * @return
 * @throws Exception
 */

2. 한줄 주석 
int number = 0; // 초기화는 반드시 해야 합니다.

3. 여러줄 주석 
/*
숫자를 뽑을때는 다음 함수를 사용해야 한다.
이유는 중앙에서 Generator 하고 있다.
*/
int getNumber();

```

# 로깅
`프로파일별로 처리 되어 있다.`

- default, local: info 레벨 출력
- dev: debug 레벨 출력
- prd: error 레벨 출력

# application 설정파일 설명
- application.yml: 공통 설정 부분 정의
- application-default: 프로파일을 정의하지 않으면 default 환경으로 구동된다.
- application-local: 로컬에서 본인만 특별하게 설정하여 처리해야 할때 사용.
- application-prd: 운영환경으로 설정해야 하는 정보를 입력하여 사용해야 할때 사용.

# 패키지 구조 
- kr.co.tworld.shop.my: root 패키지
- kr.co.tworld.shop.my.biz: 업무 컴포넌트 패키지
- kr.co.tworld.shop.my.common: 공통 컴포넌트 패키지
- kr.co.tworld.shop.my.config: 공통 설정 패키지 

# 스프링부트 시작
```
//////////////////////////////////////////////////////////////////
# 테스트 스킵 jar 생성 방법
//////////////////////////////////////////////////////////////////

1. mvnw clean install -DskipTests
2. mvnw clean install -Dmaven.test.skip=true

//////////////////////////////////////////////////////////////////
# 스프링 시작 방법 
//////////////////////////////////////////////////////////////////

mvnw spring-boot:run
java -jar target/twd-bff-app-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=local

//////////////////////////////////////////////////////////////////
# 프로파일 주입 방식
//////////////////////////////////////////////////////////////////

##################################################################
1. WebApplicationInitializer 인터페이스를 통한 방법
##################################################################
@Configuration
public class MyWebApplicationInitializer 
  implements WebApplicationInitializer {
 
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
  
        servletContext.setInitParameter(
          "spring.profiles.active", "dev");
    }
}

##################################################################
2. ConfigurableEnvironment 를 통한 방법
##################################################################
@Autowired
private ConfigurableEnvironment env;
env.setActiveProfiles("someProfile");

##################################################################
3. JVM 파라미터를 통한 방법
##################################################################
-Dspring.profiles.active=dev

##################################################################
4. 환경변수를 통한 방법
##################################################################
export spring_profiles_active=dev

##################################################################
5. Maven profiles를 통한 방법
##################################################################
<profiles>
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <spring.profiles.active>dev</spring.profiles.active>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <spring.profiles.active>prod</spring.profiles.active>
        </properties>
    </profile>
</profiles>

##################################################################
6. 메이븐 방식 -P parameter 방법
##################################################################
mvn clean package -Plocal

```

# 폴더구조
```
F:.
├─src
│  ├─main
│  │  ├─java
│  │  │  └─co
│  │  │      └─tworld
│  │  │          └─shop
│  │  │              └─my
│  │  │                  └─biz
│  │  │                      └─sample
│  │  │                          ├─controller
│  │  │                          ├─csvtodb
│  │  │                          ├─dbtodb
│  │  │                          └─simple
│  │  └─resources
│  │      ├─logger
│  │      │  └─logback
│  │      ├─static
│  │      └─templates
│  └─test
│      └─java
│          └─co
│              └─tworld
│                  └─shop
│                      └─my
```