package com.mile22t.arrowdrift;

import android.content.Intent;
import android.webkit.WebView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class GameSmokeTest {
    private String js(WebView webView, String script) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
            webView.evaluateJavascript(script, value -> { result.set(value); done.countDown(); }));
        assertTrue("JavaScript callback timed out", done.await(10, TimeUnit.SECONDS));
        return result.get();
    }

    @Test public void bundledOfflineGameLoadsAndPlays() throws Exception {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent intent = new Intent(instrumentation.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity activity = (MainActivity) instrumentation.startActivitySync(intent);
        WebView webView = activity.getGameWebView();
        long deadline = System.currentTimeMillis() + 30000;
        String ready = "false";
        while (System.currentTimeMillis() < deadline) {
            ready = js(webView, "Boolean(document.getElementById('cubeSelect')?.options.length > 100)");
            if ("true".equals(ready)) break;
            Thread.sleep(250);
        }
        assertEquals("Bundled game must initialize offline", "true", ready);
        assertEquals("\"Hard time trial\"", js(webView, "document.getElementById('level').textContent"));
        assertEquals("true", js(webView, "['2×1×1','1×2×1','1×1×2','2×2×1','2×1×2','1×2×2'].every(s=>document.getElementById('cubeSelect').textContent.includes(s))"));
        assertEquals("true", js(webView, "(()=>{const a=blocks.length;document.getElementById('hint').click();document.getElementById('release').click();return blocks.length===a-1})()"));
        Thread.sleep(500);
        assertEquals("true", js(webView, "(()=>{for(let i=0;i<5;i++)document.getElementById('shuffle').click();return shuffles===0 && document.getElementById('shuffle').disabled})()"));
        assertEquals("true", js(webView, "(()=>{document.getElementById('hint').click();document.getElementById('hint').click();return hints===0 && document.getElementById('hint').disabled && startedAt!==null})()"));
        assertEquals("true", js(webView, "(()=>{localStorage.setItem('android-smoke','ok');return localStorage.getItem('android-smoke')==='ok'})()"));
    }
}
