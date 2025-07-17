package ks55team02.customer.mypage.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {

	@NotBlank(message = "현재 비밀번호는 필수 입력 값입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.") // 길이 제한 20자로 변경
    // 영문 대소문자, 숫자, 특수문자(!@#$%^&*()) 중 각각 1가지 이상 포함하는 정규식
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,20}$",
             message = "비밀번호는 8~20자이며, 영문, 숫자, 특수문자(!@#$%^&*())를 각각 1개 이상 포함해야 합니다.")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수 입력 값입니다.")
    private String confirmNewPassword;
}
