package Uber.controller;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.FlatColorBackgroundProducer;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.DropShadowGimpyRenderer;
import cn.apiclub.captcha.gimpy.RippleGimpyRenderer;
import cn.apiclub.captcha.text.renderer.DefaultWordRenderer;
import Uber.context.CustomUserInfo;
import Uber.manager.MemberManager;
import Uber.model.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.map.IMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.session.MapSession;
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Kent Yeh
 */
@Controller
public class DefaultController {

    private static final Logger logger = LogManager.getLogger(DefaultController.class);
    private static final String LIKE = "userLike";
    private static final String DISLIKE = "userDislike";

    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;

    @Value("#{systemProperties['captcha']}")
    private String defaultCaptcha;

    @Autowired
    private MemberManager memberManager;

    /**
     * Show how to inject hazelcast instance.<br/>
     * 示範取得 hazelcast 實例.
     */
    private HazelcastInstance hazelcastInstance;

    @Autowired
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @RequestMapping("/")
    public String root(Device device, Model model) {
        if (device.isMobile()) {
            logger.debug("Connect devcie is mobile");
            model.addAttribute("device", "mobile");
            //return "mobileIndex";
        } else if (device.isTablet()) {
            logger.debug("Connect devcie is tablet");
            model.addAttribute("device", "tablet");
            //return "tabletIndex";
        } else {
            model.addAttribute("device", "normal");
            logger.debug("Connect device is normal device.");
        }
        return "index";
    }

    public static final Random RAND = new SecureRandom();

    //<editor-fold defaultstate="collapsed" desc="Grapic preparing">
    private static final List<Color> COLORS = new ArrayList<>(2);
    private static final List<Font> FONTS = new ArrayList<>(3);
    private static final char[] DEFAULT_CHARS = {'A', 'B', 'C', 'D', 'e', 'F',
        'H', 'K', 'L', 'M', 'N', '2', '3', '4', '5', '6', '7', '8', '9',
        'N', 'p', 'R', 'S', 'T', 'U', 'V', 'w', 'X', 'Y'};

    static {
        COLORS.add(Color.BLACK);
        COLORS.add(Color.BLUE);
        COLORS.add(Color.CYAN);
        COLORS.add(Color.GREEN);
        COLORS.add(Color.MAGENTA);
        COLORS.add(Color.ORANGE);
        COLORS.add(Color.PINK);
        COLORS.add(Color.RED);
        COLORS.add(Color.YELLOW);
        FONTS.add(new Font("Times New Roman", Font.BOLD, 24));
        FONTS.add(new Font("Courier", Font.BOLD, 24));
        FONTS.add(new Font("Arial", Font.ITALIC, 24));
    }

    private String getChptcha(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(DEFAULT_CHARS[RAND.nextInt(DEFAULT_CHARS.length)]);
        }
        return sb.toString();
    }

    private Color getRandColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + RAND.nextInt(bc - fc);
        int g = fc + RAND.nextInt(bc - fc);
        int b = fc + RAND.nextInt(bc - fc);
        return new Color(r, g, b);
    }
    //</editor-fold>

    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public ResponseEntity<byte[]> retrieveImage(HttpSession session,
            @RequestParam(name = "key", defaultValue = "captcha") String cpkey,
            @RequestParam(name = "len", defaultValue = "4") final int len) throws Exception {
        Captcha captcha = new Captcha.Builder(80, 36)
                //<editor-fold defaultstate="collapsed" desc="draw">
                .addNoise((BufferedImage bi) -> {
                    Graphics2D g = bi.createGraphics();
                    int h = bi.getHeight();
                    int w = bi.getWidth();
                    g.setColor(getRandColor(160, 200));
                    for (int i = 0; i < 155; i++) {
                        int x = RAND.nextInt(w);
                        int y = RAND.nextInt(h);
                        int xl = RAND.nextInt(10);
                        int yl = RAND.nextInt(4);
                        g.drawLine(x, y, x + xl, y + yl);
                    }
                })
                .addText(() -> {
                    //<editor-fold defaultstate="collapsed" desc="Random Text Generate">
                    String val = defaultCaptcha == null || defaultCaptcha.trim().isEmpty() ? getChptcha(len) : defaultCaptcha.trim();
                    session.setAttribute(cpkey, val);
                    return val;
                    //</editor-fold>
                }, new DefaultWordRenderer(COLORS, FONTS))
                .gimp(RAND.nextBoolean() ? new RippleGimpyRenderer() : new DropShadowGimpyRenderer())
                .addBorder()
                .addBackground(RAND.nextBoolean() ? new FlatColorBackgroundProducer() : new GradiatedBackgroundProducer())
                //</editor-fold>
                .build();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(captcha.getImage(), "png", baos);
            byte[] data = baos.toByteArray();
            MultiValueMap<String, String> properties = new LinkedMultiValueMap<>();
            properties.add("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            properties.add("Content-Type", MediaType.IMAGE_PNG_VALUE);//如果用produces在舊版FF會有產生406錯誤
            return new ResponseEntity<>(data, new HttpHeaders(properties), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
    public String login(Model model, Principal principal) throws Exception {
        if (principal == null) {
            model.addAttribute("members", memberManager.findAvailableUsers());
        }
        return "login";
    }

    private String getPrincipalId(Principal principal) {
        Object p = principal instanceof AbstractAuthenticationToken ? ((AbstractAuthenticationToken) principal).getPrincipal() : principal;
        return p instanceof CustomUserInfo ? ((CustomUserInfo) p).getUsername() : principal.getName();
    }

    /**
     * Response all users' data as json.
     *
     * @return
     * @throws java.lang.Exception
     */
    @RequestMapping(value = "/admin/users", method = RequestMethod.POST,
            headers = "Accept=*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String listuser() throws Exception {
        List<Member> users = memberManager.findAllUsers();
        if (users == null || users.isEmpty()) {
            return Json.createObjectBuilder().add("total", 0).add("users",
                    Json.createArrayBuilder()).build().toString();
        } else {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (Member user : users) {
                jab.add(Json.createObjectBuilder()
                        .add("account", user.getAccount())
                        .add("name", user.getName())
                        .add("birthday", String.format("%tF", user.getBirthday())));
            }
            return Json.createObjectBuilder()
                    .add("total", users.size())
                    .add("users", jab).build().toString();
        }
    }

    /**
     * Response admins'/users' data as json.
     *
     * @return
     * @throws java.lang.Exception
     */
    @RequestMapping(value = "/admin/adminOrUsers", method = RequestMethod.POST,
            headers = "Accept=*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String listAdminUsers() throws Exception {
        List<Member> users = memberManager.findAdminUser();
        if (users == null || users.isEmpty()) {
            return Json.createObjectBuilder().add("total", 0).add("users",
                    Json.createArrayBuilder()).build().toString();
        } else {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (Member user : users) {
                jab.add(Json.createObjectBuilder()
                        .add("account", user.getAccount())
                        .add("name", user.getName())
                        .add("birthday", String.format("%tF", user.getBirthday())));
            }
            return Json.createObjectBuilder()
                    .add("total", users.size())
                    .add("users", jab).build().toString();
        }
    }

    /**
     * Member's editor form.
     *
     * @param member
     * @param model
     * @return
     */
    @RequestMapping(value = "/member/edit/{member}", method = RequestMethod.GET,
            headers = "Accept=*/*", produces = "text/html;charset=UTF-8")
    public String editMember(@PathVariable Member member, Model model) {
        model.addAttribute("member", member);
        return "memberEditor";
    }

    /**
     * update member data.
     *
     * @param member
     * @param model
     * @return
     */
    @RequestMapping(value = "/member/update", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    public String updateMember(//@PathVariable("member") Member orimember, 
            @Valid @ModelAttribute Member member, Model model) {
        try {
            model.addAttribute("member", member);
            memberManager.updateMember(member);
            model.addAttribute("hint", messageAccessor.getMessage("dataUpdated"));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            model.addAttribute("errorMsg", ex.getMessage());
        }
        return "memberEditor";
    }

    /**
     * display current user's info.
     *
     * @param request
     * @param principal
     * @return
     * @throws java.lang.Exception
     */
    @RequestMapping("/user/myinfo")
    public String myinfo(HttpServletRequest request, Principal principal, Model model) throws Exception {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken upat = (UsernamePasswordAuthenticationToken) principal;
            CustomUserInfo cui = (CustomUserInfo) upat.getPrincipal();
            model.addAttribute("member", cui.getMember());
            //Alternative approach,另外一種作法
            //request.setAttribute("member", memberManager.findMemberByPrimaryKey(getPrincipalId(principal)));
        } else {
            request.setAttribute("member", memberManager.findByPrimaryKey(getPrincipalId(principal)));
        }
        model.addAttribute("like", hazelcastInstance.getCPSubsystem().getAtomicLong(LIKE).get());
        model.addAttribute("dislike", hazelcastInstance.getCPSubsystem().getAtomicLong(DISLIKE).get());
        return "index";
    }

    @RequestMapping(value = "/user/like", method = RequestMethod.POST,
            headers = "Accept=*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String userLike() throws Exception {
        IAtomicLong likeCount = hazelcastInstance.getCPSubsystem().getAtomicLong(LIKE);
        long likes = likeCount.addAndGet(1);
        return Json.createObjectBuilder().add("count", likes).build().toString();
    }

    @RequestMapping(value = "/user/dislike", method = RequestMethod.POST,
            headers = "Accept=*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String userDislike() throws Exception {
        IAtomicLong dislikeCount = hazelcastInstance.getCPSubsystem().getAtomicLong(DISLIKE);
        long dislikes = dislikeCount.addAndGet(1);
        return Json.createObjectBuilder().add("count", dislikes).build().toString();
    }

    /**
     * Only adminstrator could display any user's info.
     *
     * @param account
     * @param request
     * @return
     */
    @RequestMapping("/admin/user/{account}")
    public String userinfo(@PathVariable Member account, HttpServletRequest request) {
        if (account != null) {
            request.setAttribute("member", account);
        }
        return "index";
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllException(Exception ex) {

        ModelAndView model = new ModelAndView("error");
        model.addObject("exception", ex);

        return model;

    }
}
