package file.upload.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import file.upload.vo.ImgUploadVO;

public class ImageUploadServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 이미지를 저장할 경로
		// 실행환경과 운영환경은 다를 수 있다. ==> 임시 디렉토리
		ServletContext ctx = getServletContext();
//		String imageDir = ctx.getRealPath("/up_images"); // "/" app root 실제
															// 파일경로를 알려달라.
		//String temDir = ctx.getInitParameter("tempdir");// 업로드된 파일을 저장할 임시 디렉토리

		
		// 1. DiskFileItemFactory 객체를 생성 - 임시 저장소 정보 설정
		//DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024, new File(ctx.getInitParameter("tempdir")));

		
		// 2. ServletFileUpload 객체 생성 - DiskFileItemFactory 객체를 전달.
		ServletFileUpload upload = new ServletFileUpload
				(new DiskFileItemFactory(1024 * 1024, new File(ctx.getInitParameter("tempdir"))));


		// 3. 요청파라미터 조회작업]
		ImgUploadVO vo = new ImgUploadVO();
		List<String> imageName = new ArrayList<>();//업로드된 파일명을 저장할 List 변수
		try {
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem item : list) {

				String reqName = item.getFieldName();// 요청 파라미터 이름 조회

				if (item.isFormField()) {// 일반 요청 파라미터(String)
					/*if(reqName.equals("comment")){
						vo.setComment(reqValue);
					}else{
						vp.set
					}*/
					String reqValue = item.getString("UTF-8");// 요청 파라미터 조회.
					// 인코딩설정 반드시해야한다

					//vo에 요청 파라미터set
					if(reqName.equals("uploader")){
						vo.setUploader(reqValue);
					}else if(reqName.equals("comment")){
						vo.setComment(reqValue);
					}
				} else {// 파일 요청 파라미터(input-type ="file")
					String fileName = item.getName();// 업로드된 파일명 조회
					
					fileName= UUID.randomUUID().toString();
								//중복되지 않는 문자열을 만들어서 파일명으로 쓴다.
					
					
					// 업로드된 파일이 있는지 체크 - getSize() : long - 업로드된 파일의 크기
					if (item.getSize() != 0) {
						// 1. 임시경로의 파일을 최종 경로로 이동
						item.write(new File(ctx.getRealPath("/up_images"), fileName));
						// 2. 임시경로의 파일을 삭제
						item.delete();
						imageName.add(fileName);
					}
				}
			}//end of for 문
			vo.setImageNames(imageName);//vo에 업로드된 파일명들 설정
			// =========================요청 파라미터 조회 ===================
			//===> Model 호출해서 Business Logic 처리.
			request.setAttribute("result", vo);
			
			//응답처리
			request.getRequestDispatcher("/upload_result.jsp").forward(request, response);
		} catch (Exception e) {
			// 에러 처리 페이지로 이동.
			e.printStackTrace();
			throw new ServletException(e);// 예외 처리를 톰캣에게 맡긴다.
		}
	}
}
