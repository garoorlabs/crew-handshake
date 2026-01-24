package com.crewhandshake.features.foreman.api;

import com.crewhandshake.features.foreman.service.TodayBoardService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/foreman/today")
public class ForemanTodayController {
  private final TodayBoardService todayBoardService;

  public ForemanTodayController(TodayBoardService todayBoardService) {
    this.todayBoardService = todayBoardService;
  }

  @GetMapping
  public TodayBoardResponse getToday(@RequestParam String date, @RequestParam UUID crewId) {
    return todayBoardService.getTodayBoard(date, crewId);
  }
}
