package com.localgig.alert;

import jakarta.validation.constraints.Size;
import java.util.List;

public record JobAlertRequest(@Size(max = 200) String keywords, @Size(max = 120) String location,
                              List<@Size(max = 80) String> skills) { }
