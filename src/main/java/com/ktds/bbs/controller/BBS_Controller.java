package com.ktds.bbs.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.ktds.bbs.service.BBSService;
import com.ktds.bbs.vo.BoardVO;


@Controller
public class BBS_Controller {
// Git Ώ¬½ΐΑί

	@Autowired
	BBSService bbsService;
	
	HttpSession session;
	ModelAndView mav;

	@RequestMapping("/")
	public ModelAndView home() {
		mav.setViewName("redirect:/bbs2/list.ktds");
		return mav;
	}

	@RequestMapping("/list.ktds")
	public ModelAndView list(HttpServletRequest req) {

		mav = new ModelAndView();

		// pageNum κ°μ΄ null ????λ ?μ??κΈ°λ₯???¬μ©?λ €λ©?@RequestParam ???¬μ©?λ©΄ ?λ??
		// ?΄μ ??@RequestParam ? null ?€λ©΄ 400λ²??λ¬κ° λ°λ‘ ?¬λ€.
		String pageNum = null;
		pageNum = req.getParameter("pageNum");

		HashMap<String, Object> hs = bbsService.list(pageNum);

		mav.addObject("count", hs.get("count"));
		mav.addObject("pageNum", hs.get("pageNum"));
		mav.addObject("pageCode", hs.get("pageCode"));
		mav.addObject("articleList", hs.get("articleList"));

		mav.setViewName("list");
		return mav;
	}


	@RequestMapping("/content.ktds")
	public ModelAndView content(
			@RequestParam("article_num") String article_num,
			@RequestParam("pageNum") String pageNum) {

		mav = new ModelAndView();
		BoardVO article = bbsService.content(article_num);
		mav.addObject("article", article);
		mav.addObject("pageNum", pageNum);
		return mav;
	}
	
	@RequestMapping("/download.ktds")
	public ModelAndView download(@RequestParam("fname") String fname) {
		String realFolder = "d:/upload/";
		mav = new ModelAndView();

		mav.addObject("fileName", new File(realFolder + fname));
		mav.setViewName("downloadView");
		return mav;
	}

	@RequestMapping("/login.ktds")
	public ModelAndView login(HttpServletRequest req,
			@RequestParam("id") String id, @RequestParam("password") String pass) {
		mav = new ModelAndView();

		session = req.getSession();

		int status = bbsService.login(id, pass);

		if (status == 1) {
			session.setAttribute("id", id);
		} else if (status == 2) {
			mav.addObject("result", "?¨μ€?λ ?λ¦?);
		} else {
			mav.addObject("result", "?μ΄??μ‘΄μ¬?μ? ?μ");
		}

		if (session.getAttribute("logined") == "ok") {
			mav.setView(new RedirectView("write.ktds"));
		} else {
			mav.setView(new RedirectView("list.ktds"));
		}

		return mav;
	}

	@RequestMapping("/logout.ktds")
	public ModelAndView logout(HttpServletRequest req) {

		bbsService.logout(req);
		mav = new ModelAndView();
		mav.setViewName("redirect:/list.ktds");
		return mav;
	}


	@RequestMapping("/delete.ktds")
	public ModelAndView delete(@RequestParam("article_num") String article_num,
			@RequestParam("pageNum") String pageNum) {
		bbsService.delete(article_num);

		return new ModelAndView("redirect:list.ktds?pageNum=" + pageNum);
	}

	@RequestMapping("/writeForm.ktds")
	public ModelAndView writeForm(HttpServletRequest req,
			HttpServletResponse rsp) {
		HttpSession session = req.getSession();

		if (session.getAttribute("id") != null) {
			return new ModelAndView("writeForm");
		} else {
			session.setAttribute("logined", "ok");
			return new ModelAndView("LoginPage");
		}
	}

	@RequestMapping("/write.ktds")
	public ModelAndView write(HttpServletRequest req,
			@ModelAttribute("article") BoardVO article)
			throws IllegalStateException, IOException {

		session = req.getSession();

		// μ£Όμ? μ 
		// MultipartResolver???μΌ???λ‘???μ? ?μ κ²½μ° null ??λ¦¬ν΄?λ κ²μ΄ ?λ??
		// "" (κ³΅λ°±) ???°λ°?΄μ€ κ·Έλ???μΌ?λ‘?λ? ?μ? ?μ??NullPointerException??λ°μ?μ? ?μ

		MultipartFile uploadfile = article.getuploadFile(); // ?¬κΈ°?λ Null

		String fname = uploadfile.getOriginalFilename(); // ?¬κΈ°?λ ""
		if (fname.equals("")) {
			article.setFname(null);
		} else {
			article.setFname(fname);

			// 1. FileOutputStream ?¬μ©
			// byte[] fileData = file.getBytes();
			// FileOutputStream output = new FileOutputStream("C:/images/" +
			// fileName);
			// output.write(fileData);

			// 2. transferTo ?¨μ ?¬μ©
			uploadfile.transferTo(new File("d:/upload/" + fname));
		}

		article.setId((String) session.getAttribute("id"));

		bbsService.write(article);

		return new ModelAndView("redirect:list.ktds");
	}

	@RequestMapping("/modifyForm.ktds")
	public ModelAndView modifyForm(
			@RequestParam("article_num") String article_num,
			@RequestParam("pageNum") String pageNum) {

		mav = new ModelAndView();

		mav.addObject("article", bbsService.modifyForm(article_num));
		mav.addObject("pageNum", pageNum);
		mav.setViewName("modifyForm");
		return mav;
	}

	@RequestMapping("/modify.ktds")
	public ModelAndView modify(HttpServletRequest req,
			@ModelAttribute("article") BoardVO article)
			throws IllegalStateException, IOException {

		session = req.getSession();

		MultipartFile uploadfile = article.getuploadFile(); // ?¬κΈ°?λ Null

		String fname = uploadfile.getOriginalFilename(); // ?¬κΈ°?λ ""
		System.out.println("fname" + fname);
		if (fname.equals("")) {

		} else {
			article.setFname(fname);

			// 1. FileOutputStream ?¬μ©
			// byte[] fileData = file.getBytes();
			// FileOutputStream output = new FileOutputStream("C:/images/" +
			// fileName);
			// output.write(fileData);

			// 2. transferTo ?¨μ ?¬μ©
			uploadfile.transferTo(new File("d:/upload/" + fname));
		}

		article.setId((String) session.getAttribute("id"));

		bbsService.modify(article);

		String url = "redirect:/content.ktds?article_num="
				+ req.getParameter("article_num") + "&pageNum="
				+ req.getParameter("pageNum");

		return new ModelAndView(url);
	}

	@RequestMapping("/replyForm.ktds")
	public ModelAndView replyForm(HttpServletRequest req) {

		mav = new ModelAndView();

		mav.addObject("pos", req.getParameter("pos"));
		mav.addObject("article_num", req.getParameter("article_num"));
		mav.addObject("group_id", req.getParameter("group_id"));
		mav.addObject("depth", req.getParameter("depth"));
		mav.addObject("pageNum", req.getParameter("pageNum"));

		mav.setViewName("replyForm");
		return mav;
	}

	@RequestMapping("/reply.ktds")
	public ModelAndView reply(HttpServletRequest req,
			@RequestParam("pageNum") String pageNum,
			@ModelAttribute("article") BoardVO article)
			throws IllegalStateException, IOException {
		session = req.getSession();

		MultipartFile uploadfile = article.getuploadFile(); // ?¬κΈ°?λ Null

		String fname = uploadfile.getOriginalFilename(); // ?¬κΈ°?λ ""
		if (fname.equals("")) {
			article.setFname(null);
		} else {
			article.setFname(fname);

			// 1. FileOutputStream ?¬μ©
			// byte[] fileData = file.getBytes();
			// FileOutputStream output = new FileOutputStream("C:/images/" +
			// fileName);
			// output.write(fileData);

			// 2. transferTo ?¨μ ?¬μ©
			uploadfile.transferTo(new File("d:/upload/" + fname));
		}

		article.setId((String) session.getAttribute("id"));

		bbsService.reply(article);

		String url = "redirect:list.ktds?pageNum=" + pageNum;
		return new ModelAndView(url);
	}
}