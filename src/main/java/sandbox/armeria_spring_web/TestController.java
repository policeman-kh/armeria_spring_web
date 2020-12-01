package sandbox.armeria_spring_web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {
    @GetMapping
    public ModelAndView index() {
        return new ModelAndView("index")
                .addObject("msg", "Hello world.");
    }

    @GetMapping("long_execution")
    public ModelAndView longExecution(){
        // The codes to reproduce response time out error.
        try {
            Thread.sleep(50_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ModelAndView("index")
                .addObject("msg", "Hello world.");
    }
}
