package kr.spring.board.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import kr.spring.board.domain.BoardVO;
import kr.spring.board.service.BoardService;
import kr.spring.util.PagingUtil;

@Controller
public class BoardController {
	private Logger log = Logger.getLogger(this.getClass());
	private int rowCount = 10;
	private int pageCount = 10;
	
	//자바빈(VO) 초기화
	@ModelAttribute
	public BoardVO initCommand() {
		return new BoardVO();
	}

	@Resource
	private BoardService boardService;
	
	//목록
	@RequestMapping("/board/list.do")
	public ModelAndView process(@RequestParam(value="pageNum",defaultValue="1") int currentPage,
			                    @RequestParam(value="keyfield",defaultValue="") String keyfield,
			                    @RequestParam(value="keyword",defaultValue="") String keyword) {
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		map.put("keyfield", keyfield);
		map.put("keyword", keyword);
		
		//게시판의 총 레코드 수 또는 검색 레코드 수 반환
		int count = boardService.selectRowCount(map);
		if(log.isDebugEnabled()) {
			log.debug("<<count>> : "+ count + keyfield + keyword);
		}
		
		//페이징 처리
		PagingUtil page = new PagingUtil(keyfield,keyword,currentPage,count,rowCount,pageCount,"list.do");
		map.put("start", page.getStartCount());
		map.put("end", page.getEndCount());
		
		List<BoardVO> list = null;
		if(count > 0) {
			list = boardService.selectList(map);
		}
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("boardList");
		mav.addObject("count",count);
		mav.addObject("list",list);
		mav.addObject("pagingHtml",page.getPagingHtml());
		
		return mav;
	}
	
	//글쓰기 
	@RequestMapping(value="/board/write.do",method=RequestMethod.GET)
	public String form() {
		return "boardWrite";
	}
	//글쓰기 처리
	@RequestMapping(value="/board/write.do",method=RequestMethod.POST)
	public String submit(@Valid BoardVO boardVO,BindingResult result,HttpServletRequest request,HttpSession session) {
		
		if(log.isDebugEnabled()) {
			log.debug("글쓰기 <<BoardVO>> :"+boardVO);
		}
		//유효성 체크 결과 에러가 있으면 폼을 호출
		if(result.hasErrors()) {
			return "boardWrite";
		}
		//mem_num
		boardVO.setMem_num((Integer)session.getAttribute("user_num"));
		//ip 반환
		boardVO.setIp(request.getRemoteAddr());
		if(log.isDebugEnabled()) {
			log.debug("글쓰기 <<BoardVO>> :"+boardVO);
		}
		//글 등록
		boardService.insert(boardVO);
		
		return "redirect:/board/list.do";
	}
	
	//글 상세
	@RequestMapping("/board/detail.do")
	public ModelAndView process(@RequestParam("num") int num) {
		//로그표시
		if(log.isDebugEnabled()) {
			log.debug("<<num>> : " + num);
		}
		//조회수 증가
		boardService.updateHit(num);
		
		BoardVO board = boardService.selectBoard(num);
		
								//뷰 이름            속성명       속성값
		return new ModelAndView("boardView","board",board);
	}
	//파일 다운로드
	@RequestMapping("/board/file.do")
	public ModelAndView download(@RequestParam("num") int num) {
		BoardVO board = boardService.selectBoard(num);
		ModelAndView mav = new ModelAndView();
		mav.setViewName("downloadView");//jsp가 아니라 servlet-context.xml에서 설정한 객체를 소환한다. 
		//파일 정보
		mav.addObject("downloadFile",board.getUploadfile());
		mav.addObject("filename",board.getFilename());
		
		
		return mav;
	}
	
	//이미지 처리
	@RequestMapping("/board/imageView.do")
	public ModelAndView viewImage(@RequestParam("num") int num) {
		BoardVO board = boardService.selectBoard(num);
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("imageView");
		mav.addObject("imageFile",board.getUploadfile());
		mav.addObject("filename", board.getFilename());
		
		return mav;
	}
	//글 수정 폼 호출
	@RequestMapping(value="/board/update.do",method=RequestMethod.GET)
	public String form(@RequestParam("num") int num, Model model) {
		
		BoardVO boardVO = boardService.selectBoard(num);
		model.addAttribute("boardVO",boardVO);
		
		if(log.isDebugEnabled()) {
			log.debug("<<글수정 boardVO 전달>> :" + boardVO);
		}
		
		return "boardModify";
	}
	//글 수정 처리
	@RequestMapping(value="/board/update.do",method=RequestMethod.POST)
	public String submitUpdate(@Valid BoardVO boardVO,BindingResult result,HttpServletRequest request) {
		
		//로그표시
		if(log.isDebugEnabled()) {
			log.debug("<<수정될 BoardVO>> : " + boardVO );
		}
		//유효성 체크 결과에러가 있으면 폼 호출
		if(result.hasErrors()) {
			return "boardModify";
		}
		
		boardVO.setIp(request.getRemoteAddr());
		
		boardService.update(boardVO);
		
		return "redirect:/board/list.do";
	}
	//글 삭제
	@RequestMapping("/board/delete.do")
	public String submit(@RequestParam("num") int num) {
		
		//로그 표시
		if(log.isDebugEnabled()) {
			log.debug("<<num>> : " + num);
		}
		
		//글 삭제
		boardService.delete(num);
		
		return "redirect:/board/list.do";
	}
	
	
}















