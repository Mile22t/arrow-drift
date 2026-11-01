package com.mile22t.arrowdrift;

import android.content.Intent;
import android.webkit.WebView;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import org.json.JSONArray;
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

    private void touch(long downTime, int action, float[][] points) throws Exception {
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[points.length];
        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[points.length];
        for (int i = 0; i < points.length; i++) {
            properties[i] = new MotionEvent.PointerProperties();
            properties[i].id = i;
            properties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
            coords[i] = new MotionEvent.PointerCoords();
            coords[i].x = points[i][0]; coords[i].y = points[i][1];
            coords[i].pressure = 1; coords[i].size = 1;
        }
        MotionEvent event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action,
            points.length, properties, coords, 0, 0, 1, 1, 0, 0,
            InputDevice.SOURCE_TOUCHSCREEN, 0);
        InstrumentationRegistry.getInstrumentation().sendPointerSync(event);
        event.recycle();
        Thread.sleep(100);
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
        assertEquals("true", js(webView, "document.querySelector('aside')===null && document.querySelector('footer')===null"));
        JSONArray bounds = new JSONArray(js(webView,
            "(()=>{const r=document.getElementById('canvas').getBoundingClientRect();return [r.left+r.width/2,r.top+r.height/2,window.innerWidth]})()"));
        int[] location = new int[2];
        instrumentation.runOnMainSync(() -> webView.getLocationOnScreen(location));
        float factor = webView.getWidth() / (float) bounds.getDouble(2);
        float cx = location[0] + (float) bounds.getDouble(0) * factor;
        float cy = location[1] + (float) bounds.getDouble(1) * factor;
        String piecesBeforeGesture = js(webView, "blocks.length");
        long downTime = SystemClock.uptimeMillis();
        touch(downTime, MotionEvent.ACTION_DOWN, new float[][]{{cx-60,cy}});
        touch(downTime, MotionEvent.ACTION_POINTER_DOWN | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT), new float[][]{{cx-60,cy},{cx+60,cy}});
        touch(downTime, MotionEvent.ACTION_MOVE, new float[][]{{cx-100,cy},{cx+100,cy}});
        touch(downTime, MotionEvent.ACTION_POINTER_UP | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT), new float[][]{{cx-100,cy},{cx+100,cy}});
        touch(downTime, MotionEvent.ACTION_UP, new float[][]{{cx-100,cy}});
        Thread.sleep(300);
        assertTrue("Real two-finger input must zoom the puzzle", Double.parseDouble(js(webView, "zoom")) > 1.4);
        assertEquals("Pinching must not release a piece", piecesBeforeGesture, js(webView, "blocks.length"));
        js(webView, "document.getElementById('home').click()");

        assertEquals("true", js(webView, "['2×1×1','1×2×1','1×1×2','2×2×1','2×1×2','1×2×2'].every(s=>document.getElementById('cubeSelect').textContent.includes(s))"));
        assertEquals("true", js(webView, "(()=>{const a=blocks.length;document.getElementById('hint').click();document.getElementById('release').click();return blocks.length===a-1})()"));
        Thread.sleep(500);
        assertEquals("true", js(webView, "(()=>{for(let i=0;i<5;i++)document.getElementById('shuffle').click();return shuffles===0 && document.getElementById('shuffle').disabled})()"));
        assertEquals("true", js(webView, "(()=>{document.getElementById('hint').click();document.getElementById('hint').click();return hints===0 && document.getElementById('hint').disabled && startedAt!==null})()"));
        assertEquals("true", js(webView, "(()=>{localStorage.setItem('android-smoke','ok');return localStorage.getItem('android-smoke')==='ok'})()"));
    }
}
