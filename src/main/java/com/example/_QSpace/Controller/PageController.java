package com.example._QSpace.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/solutions")
    public String solutionsPage() {
        return "solutions";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }
}

