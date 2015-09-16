package org.tevya;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.tevya.org.tevya.model.CreationRequest;
import org.tevya.org.tevya.model.LinkDefinition;
import org.tevya.org.tevya.repo.LinkDefinitionRepository;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

@Controller
@EnableAutoConfiguration
@Import(RepositoryConfig.class)
public class MainController {

    @Autowired
    protected LinkDefinitionRepository  linkDefinitionRepository;

    @Autowired
    LinkGenerator linkGenerator;

    @Autowired
    LinkDefinitionRepository repository;

    private String accessKey;

    Logger  logger = Logger.getLogger(MainController.class.getName());

    private UrlValidator validator;

    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Another URL shortener.";
    }

    @PostConstruct
    public void postConstruction() throws Exception {
        final Random rand = new Random(System.currentTimeMillis());

        String[] acceptedSchemes = {"http","https"};
        validator = new UrlValidator(acceptedSchemes);
        int keyValue = rand.nextInt(1000000) + 1;
        this.accessKey = Integer.toString(keyValue,36);
        logger.info(String.format("********** Access Key is k%s **********",accessKey));
        repository.initialize();
    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainController.class, args);
    }

    @RequestMapping(value="/k{accessKey}/lnk", method=RequestMethod.DELETE)
    public void resetRepository (
            @PathVariable String accessKey,
            HttpServletResponse response) {
        if (!StringUtils.equals(accessKey,this.accessKey)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        repository.deleteAll();
    }


    @RequestMapping(value="/k{accessKey}/lnk", method=RequestMethod.POST)
    public  @ResponseBody String create(
            @PathVariable String accessKey,
            @RequestParam(value = "alias", required = false) String alias,
            @RequestBody CreationRequest request,
            HttpServletResponse response) throws IOException {

        if (!StringUtils.equals(accessKey,this.accessKey)){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return "Access denied";
        }

        if (!validator.isValid(request.getUrl()))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL is invalid");
            return "";
        }

        boolean createAlias = !StringUtils.isEmpty(alias);
        LinkDefinition linkDefinition = new LinkDefinition();

        try {
            String newKey = linkGenerator.getNewKey(repository);
            linkDefinition.setKey(newKey);
            linkDefinition.setAlias(createAlias ? alias.toLowerCase() : null);
            linkDefinition.setTargetUrl(request.getUrl());
            if (repository.add(linkDefinition)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("text/plain");
                String tinyLink = String.format("http://%s%s", request.getServiceDomain(), formatLink(createAlias, linkDefinition));
                return tinyLink;
            }
            else {
                response.sendError(HttpServletResponse.SC_CONFLICT,String.format("%s already exists", createAlias ? "Alias" : "Link"));
                return "";
            }
        } catch (Exception e) {
            logger.severe(String.format("Internal Error: %s",e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "System failure";
        }
    }

    @RequestMapping(value="/l/{key}")
    public void redirectByKey(@PathVariable String key,
                              HttpServletResponse response) throws IOException {
        LinkDefinition ld = repository.getByKey(key);
        redirect(response, ld == null ? null : ld.getTargetUrl());
    }

    @RequestMapping(value="/l/{key}", method=RequestMethod.DELETE)
    public void deleteByKey(@PathVariable String key,
                            HttpServletResponse response) throws IOException {
            repository.deleteByKey(key);
    }

    @RequestMapping(value="/a/{alias}")
    public void redirectByAlias(@PathVariable String alias,
                                HttpServletResponse response) throws IOException {
        LinkDefinition ld = repository.getByAlias(alias);
        redirect(response, ld == null ? null : ld.getTargetUrl());
    }

    @RequestMapping(value="/a/{alias}", method=RequestMethod.DELETE)
    public void deleteByAlias(@PathVariable String alias,
                            HttpServletResponse response) throws IOException {
            repository.deleteByAlias(alias);
    }

    private void redirect(HttpServletResponse response, String url) throws IOException {
        if (url == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.sendRedirect(url);
        }
    }

    private String formatLink(boolean createAlias, LinkDefinition linkDefinition) {
        return createAlias
                ? String.format("/a/%s",linkDefinition.getAlias())
                : String.format("/l/%s",linkDefinition.getKey());
    }
}
