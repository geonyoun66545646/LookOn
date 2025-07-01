package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatusOptionMapping {
    private String gdsSttsNo;
    private String optVlNo;
    private String creatrNo;
    private String delUserNo;
    private LocalDateTime crtYmd;
    private LocalDateTime inactvtnDt;
    private boolean actvtnYn;
}