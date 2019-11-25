package jp.ac.asojuku.myrollingball

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
    ,SensorEventListener//各種センサーの反応をOSから受け取るためのインターフェース
    ,SurfaceHolder.Callback{//サーフェースViewを実装するための窓口Holderのコールバックインターフェース

    //インスタンスプロパティ変数
    private var surfaceWidth:Int = 0; //サーフェースビュー の幅
    private var surfaceHeight:Int = 0//サーフェースビューの高さ

    private val radius = 50.0f;//ボールの半径
    private var coef = 100.0f//ボールの移動量を計算するための係数

    private var ballX:Float = 0f;//ボールのx座標
    private var ballY:Float = 0f;//ボールのy座標
    private var vx:Float = 0f;//ボールのX座標の重力加速度
    private var vy:Float = 0f;//ボールのY座標の重力加速度
    private var time:Long = 0L;//前回の時間を記録する変数




    //画面の生成時のイベントコールバックメソッド
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)//画面レイアウトを設定
        //サーフェースホルダーをサーフェースView部品から取得
        val holder = surfaceView.holder
        //サーフェースホルダーのコールバックに自クラスへの通知を追加
        holder.addCallback(this);
    }

    //画面の表示・再表示のイベントコールバックメソッド
    override fun onResume() {
        super.onResume()
        button.setOnClickListener{
            //ボールを初期位置に戻し、加速度を0にする
            ballX = (surfaceWidth/2).toFloat();
            ballY = (surfaceHeight/8).toFloat();
            textView.setText(R.string.ganbare_text)
            vx=0f
            vy=0f
            coef=100f
        }

    }

    //画面が非表示になる時に呼ばれるコールバックメソッド
    override fun onPause(){
        super.onPause();

    }

    //SensorEventListenerの必須overrideメソッド(センサーの精度が変わるとコールバックされる)
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //今回は何もしない
    }

    //SensorEventListenerの必須overrideメソッド(センサーの取得値が変わるとコールバックされる)
    override fun onSensorChanged(event: SensorEvent?) {
        //引数(イベント)の中身が何もなかったら何もせず終了
        if(event == null){ return; }

        //センサーが変わった時に、ボールを描画するための情報を計算する
        //一番最初のセンサー検知の時の、初期時間を取得
        if(time==0L){time=System.currentTimeMillis()}//最初は現在のミリ秒システム時刻を設定
        //eventのセンサー種別が加速度センサーだったら以下を実行
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //センサーの取得した値(左右の変化：x,上下の変化：y)を変数に代入
            val x = event.values[0]*-1;//横左右の値
            val y = event.values[1];//上下の値

            //前回の時間(time)からの経過時間を計算(今の時間-前回の時間=経過時間)
            var t = ((System.currentTimeMillis()) - time).toFloat();//計算結果はFloat型にしておく
            //timeに今の時間を「前の時間として」保存しなおす
            time = System.currentTimeMillis();
            t /= 1000.0f;//ミリ秒単位を秒単位になおすために1000でわる

            //移動距離を計算(ボールの座標をどれくらい動かすか)
            val dx = ((vx*t)+(x*t*t))/1.2f;//xの移動すべき距離
            val dy = ((vy*t)+(y*t*t))/2.0f;//yの移動すべき距離
            ballX += (dx*coef);//ボールの新しいx座標
            ballY += (dy*coef);//ボールの新しいy座標
            //今瞬間の加速度を保存しなおす
            vx += (x*t);
            vy += (y+t);

            //画面の端にきたら跳ね返る処理
            //左右について
            if(ballX-radius<0 && vx<0){
                //左に向かっていてボールが左にはみ出した時
                vx = (vx*-1)/1.5f;//ボールを反転させて勢いをつける
                ballX = radius;//ボールがはみ出しているのを補正
            }else if (ballX+radius>surfaceWidth && vx>0){
                //右に向かっていてボールが右にはみ出した時
                vx = (vx*-1)/1.5f//ボールを反転させて勢いをつける
                ballX = surfaceWidth-radius;//ボールがはみ出しているのを補正
            }

            //上下について
            if(ballY-radius<0 && vy<0){
                //上に向かっていてボールが上にはみ出した時
                vy = (vy*-1)/1.5f;//ボールを反転させて勢いをつける
                ballY = radius;//ボールがはみ出しているのを補正
            }else if (ballY+radius>surfaceHeight && vy>0){
                //下に向かっていてボールが下にはみ出した時
                vy = (vy*-1)/1.5f//ボールを反転させて勢いをつける
                ballY = surfaceHeight-radius;//ボールがはみ出しているのを補正
                textView.setText(R.string.win_text)
            }
            //壁判定メソッド呼び出し
            ballCheck(2f,2f,1.25f,1.75f)
            ballCheck(8f,4f,6f,1.5f)

                //キャンバスに描画する命令
            drawCanvas()
        }



//            // センサーの値が変わった時の処理を書く
//            Log.d("TAG01","センサーの値が変わりました");
//            //引数(イベント)の中身が何もなかったら何もせず終了
//            if(event == null){ return; }
//            //加速度センサーのイベントか判定
//            if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
//                //ログに表示するための文字列を組み立てる
//                var str:String = "xの値：${event.values[0].toString()}" + "yの値：${event.values[1].toString()}" + "zの値：${event.values[2].toString()}"
//                //デバッグログに出力
//                Log.d("加速度センサー",str);
//            }
    }

    private fun ballCheck(left:Float,top:Float,right:Float,bottom:Float){
        //壁判定
        if(ballX+radius>=surfaceWidth/left && surfaceHeight/top<ballY+radius) {
            if(ballX-radius<=surfaceWidth/right && surfaceHeight/bottom>ballY-radius) {
//                coef = 0f;
//                textView.setText(R.string.lose_text)
                if(ballX+radius>=surfaceWidth/left && ballX-radius<=surfaceWidth/right)
                    vx*=-1
                if(surfaceHeight/top<ballY+radius && surfaceHeight/bottom>ballY-radius)
                    vy*=-1
            }
        }
    }

    //サーフェースが更新された時のイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        //サーフェースが変化するたびに幅と高さを設定(インスタンスに記憶しておく)
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        //ボールの初期位置を指定
        ballX = (surfaceWidth/2).toFloat();
        ballY = (surfaceHeight/8).toFloat();

    }

    //サーフェースが破棄された時のイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        //センサーマネージャーのインスタンスを取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //センサーマネージャーから登録した自クラス(画面)への通知を解除(OFF)
        sensorManager.unregisterListener(this);
    }

    //サーフェースが生成された時のイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceCreated(holder: SurfaceHolder?) {
        //センサーマネージャーのインスタンスをOSから取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //センサーマネージャーから加速度センサー(Accelermeter)を指定してそのインスタンスを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //センサーのリスナーに登録して加速度センサーの監視を開始(ONにする)
        sensorManager.registerListener(
            this,//イベントリスナー機能を持つインスタンス(今回は画面クラス。ここに通知してもらう)
            accSensor,//監視するセンサーのインスタンス(今回は加速度センサー)
            SensorManager.SENSOR_DELAY_GAME//センサーの更新通知頻度
        )
    }

    //サーフェースのキャンバスに描画する処理をまとめたメソッド
    private fun drawCanvas(){
        //キャンバスをロックして取得する
        val canvas = surfaceView.holder.lockCanvas();
        //キャンバスに背景の色を塗る
        canvas.drawColor(Color.DKGRAY);//ダークグレー
        //キャンバスに円を描いてボールにする
        canvas.drawCircle(
            ballX,//ボールのx座標
            ballY,//ボールのy座標
            radius,//ボールの半径
            Paint().apply {//Paintの匿名インスタンス
                color = Color.YELLOW;//色を黄色にする
            }
        );

        //長方形その1(赤)
        canvas.drawRect(
            surfaceWidth/2.toFloat(),
            surfaceHeight/2.toFloat(),
            surfaceWidth/1.25.toFloat(),
            surfaceHeight/1.75.toFloat(),
            Paint().apply {
                color = Color.RED
            }
        )

        canvas.drawRect(
            surfaceWidth/8.toFloat(),
            surfaceHeight/4.toFloat(),
            surfaceWidth/6.toFloat(),
            surfaceHeight/1.5.toFloat(),
            Paint().apply {
                color = Color.RED
            }
        )
        //キャンバスのロックを解除して描画(表示)
        surfaceView.holder.unlockCanvasAndPost(canvas);
    }

}
