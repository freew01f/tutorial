package com.example.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@SpringBootApplication
public class ValidationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationApplication.class, args);
    }
}

@Getter
@Setter
@ToString
@AllArgsConstructor
class ApiResultVO{
    private Integer code;
    private String message;
    private Object data;
}

@Getter
@Setter
@ToString
@AllArgsConstructor
class BaseException extends RuntimeException {
    protected Integer code;
    protected String message;

    public static final BaseException UNKNOWN_ERROR = new BaseException(1, "未知错误");
    public static final BaseException VALIDATION_ERROR = new BaseException(250, "验证错误");

}

@ControllerAdvice
@ResponseStatus(HttpStatus.OK)
class ControllerErrorHandler{

    // 日志记录
    private static final Logger logger = LoggerFactory.getLogger(ControllerErrorHandler.class);

    @ResponseBody
    @ExceptionHandler(value = BaseException.class)
    public ApiResultVO handleISSException(BaseException e) {
        return new ApiResultVO(e.getCode(), e.getMessage(), "");
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResultVO handleAccessDeniedException(MethodArgumentNotValidException e) {
//        return new ApiResultVO(BaseException.VALIDATION_ERROR.getCode(), BaseException.VALIDATION_ERROR.getMessage(), "");
        return new ApiResultVO(BaseException.VALIDATION_ERROR.getCode(), e.getBindingResult().getAllErrors().get(0).getDefaultMessage(), "");
    }
}

@Getter
@Setter
@ToString
class User{

    private Long id;
    @NotBlank(message = "登录名不能为空")
    private String username;
    @NotBlank(message = "手机不能为空")
    @Length(max = 11, min = 11, message = "手机号长度必须是11位")
    private String mobile;
    @NotBlank(message = "密码不能为空")
    @Length(max = 10, min = 7, message = "密码是7-10位")
    private String password;
}

@RestController
class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @GetMapping("/hello/{name}")
    private ApiResultVO hello(@PathVariable(value = "name", required = true) String name){
        return new ApiResultVO(0, "验证成功", "");
    }

    // curl -X POST "http://localhost:8080/user/register" -H "Content-Type: application/json" -d "{\"username\":\"hello\", \"password\":\"123456\"}" | json_pp
    @PostMapping("/user/register")
    private ApiResultVO register(@RequestBody User user){
        return new ApiResultVO(0, "注册成功", "");
    }

//    curl -X POST "http://localhost:8080/user/register-v" -H "Content-Type: application/json" -d "{\"username\":\"hello\", \"password\":\"1234651\" , \"mobile\":\"13901234567\"}" | json_pp
@PostMapping("/user/register-v")
    private ApiResultVO registerValidat(@Validated @RequestBody User user){
        return new ApiResultVO(0, "注册成功", "");
    }
}