package com.sinosun.train.utils;

import com.alibaba.fastjson.JSONObject;
import com.sinosun.train.enums.PlatformErrorCode;
import com.sinosun.train.exception.ServiceException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2019/1/15 19:46.
 *
 * @author caogu
 */
public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(15);

    /**
     * 执行HTTP请求  <p>get请求时data传null</p>
     *
     * @param url    url完整地址
     * @param method Connection.Method.GET  Connection.Method.POST
     * @param data   请求参数的JSON对象
     * @return HTTP接口返回值
     */
    public static String request(String url, Connection.Method method, JSONObject data) {
        logger.info("======request url={} data={}", url, data);
        String result;
        try {
            Connection conn = Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                    .header("Cache-Control","max-age=0")
                    .header("Connection","keep-alive")
                    .header("Cookie","_uab_collina=168819928343033606641125; _jc_save_toStation=%u4E0A%u6D77%2CSHH; _jc_save_wfdc_flag=dc; guidesStatus=off; highContrastMode=defaltMode; cursorStatus=off; _jc_save_toDate=2023-08-16; _jc_save_fromStation=%u4E0A%u9976%2CSRG; fo=qx0s397jdjfmgfg0489Yp1Wns2R9yR57qBPfIoDxX4RWpHpdCZuXiaFsGjkTe0V3HMFPUd-yPSt6R2Igtmlcyz7eDpqBb7CAbXQPsSx_mwCVGlGrwO-coAGATgAxZVkj0clCHaWi9E-bZxPzHZ-W5wWklmvZR6P4zbWLNTpyQf3nRb_4pZu-m5KNzSk; _jc_save_fromDate=2023-08-17")
                    .header("Host","kyfw.12306.cn")
                    .header("Sec-Fetch-Dest","document")
                    .header("Sec-Fetch-Mode","navigate")
                    .header("Sec-Fetch-Site","none")
                    .header("Sec-Fetch-User","?1")
                    .header("Upgrade-Insecure-Requestsr","1")
                    .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.0.0")
                    .header("sec-ch-u","\"Microsoft Edge\";v=\"117\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"117\"")
                    .header("sec-ch-ua-mobile","?0")
                    .header("sec-ch-ua-platform","\"Windows\"")
                    .followRedirects(true)
                    .ignoreContentType(true);

            if (data != null) {
                conn.requestBody(data.toJSONString());
            }
            Connection.Response response = conn.method(method).execute();

            int code = response.statusCode();
            if (code == HttpStatus.OK.value() || code == HttpStatus.FOUND.value()) {
                result = response.body();
            } else {
                logger.error("执行url={}返回非200/302值, statusCode={}", url, response.statusCode());
                throw new ServiceException(PlatformErrorCode.SERVICE_INTERNAL_ERROR);
            }
            logger.info("======request code={} result={}", code, result);
        } catch (IOException e) {
            logger.error("执行" + url + "出错, data=" + data, e);
            throw new ServiceException(PlatformErrorCode.SERVICE_INTERNAL_ERROR, e);
        }
        return result;
    }

}
