package jp.wmyt.livescheduler.app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by miyata on 2014/05/06.
 */
public class AsyncDownloadTask extends AsyncTask<String, Integer, Integer> {
    String srcFile;
    String downloadPath;

    public interface AsyncDownloadCallback {
        public static final Integer SUCCESS = 0;
        public static final Integer CANCEL = -1;
        public static final Integer NETWORK_ERROR = -2;
        public static final Integer EXCEPTION = -2;

        void preExecute();
        void callbackExecute(final int responseCode);
    }

    private AsyncDownloadCallback callback = null;
    private Context mContext = null;

    AsyncDownloadTask( Context context, String src, String dst, AsyncDownloadCallback _callback ) {
        mContext = context;
        srcFile = src;
        downloadPath = dst;
        callback = _callback;
    }

    /**
     * 前処理
     */
    @Override
    public void onPreExecute()
    {
        callback.preExecute();
    }

    /**
     * バックグラウンド処理
     */
    @Override
    protected Integer doInBackground(String... params) {
        try {
            Log.d("AsyncDownloadTask", "path:" + srcFile + " save:" + downloadPath);
            URL url = new URL( srcFile.toString() );
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

            // 実際のダウンロード処理
            httpURLConnection.setRequestMethod( "GET" );
//                httpURLConnection.setRequestProperty(
//                        "Range",
//                        String.format( "byte=%d-%d", this.nFileSizeCount, (int)this.nFileSize[ i ] )
//                );
            httpURLConnection.connect();

            // データのダウンロードを開始
            int code = httpURLConnection.getResponseCode();
            if( ( code == 200 ) || ( code == 206 ) ) {
                // HTTP 通信の内容をファイルに保存するためのストリームを生成
                InputStream inputStream = httpURLConnection.getInputStream();
//                FileOutputStream fileOutputStream = new FileOutputStream( download, true );
                FileOutputStream out = mContext.openFileOutput(downloadPath, Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(out);
                BufferedWriter writer = new BufferedWriter(osw);

                byte[] buffReadBytes = new byte[ 409600 ];
                for( int sizeReadBytes = inputStream.read( buffReadBytes); sizeReadBytes != -1; sizeReadBytes = inputStream.read( buffReadBytes ) ) {
                    // ファイルに書き出し
                    out.write( buffReadBytes, 0, sizeReadBytes );
                }
                out.close();

                return AsyncDownloadCallback.SUCCESS;
            }else{
                return AsyncDownloadCallback.NETWORK_ERROR;
            }
        } catch( MalformedURLException e ) {
            e.printStackTrace();
        } catch( ProtocolException e ) {
            e.printStackTrace();
        } catch( IOException e ) {
            e.printStackTrace();
        } finally {
//                // ダウンロードが無事に完了しているのであればリネームする
//                if( bComplete ) {
//                    if( !bCancel ) {
//                        temporary.renameTo( new File( dstDir.toString() + srcFiles[ i ].toString() ) );
//                    }
//                }
//
//            }
        }


        return AsyncDownloadCallback.EXCEPTION;
    }

    /**
     * 進捗処理
     */
    @Override
    protected void onProgressUpdate(Integer...values) {

    }

    /**
     * キャンセル
     */
    protected void onCancelled() {
        callback.callbackExecute(AsyncDownloadCallback.CANCEL);
    }
    /**
     * 後処理
     */
    @Override
    public void onPostExecute( Integer result ) {
        callback.callbackExecute(result);
    }
}
