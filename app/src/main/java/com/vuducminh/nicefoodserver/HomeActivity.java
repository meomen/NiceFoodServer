package com.vuducminh.nicefoodserver;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import com.vuducminh.nicefoodserver.adapter.PdfDocumentAdapter;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.common.CommonAgr;
import com.vuducminh.nicefoodserver.common.PDFUtils.PDFUtils;
import com.vuducminh.nicefoodserver.eventbus.CategoryClick;
import com.vuducminh.nicefoodserver.eventbus.ChangeMenuClick;
import com.vuducminh.nicefoodserver.eventbus.PrintOrderEvent;
import com.vuducminh.nicefoodserver.eventbus.ToastEvent;
import com.vuducminh.nicefoodserver.model.CartItem;
import com.vuducminh.nicefoodserver.model.FCMserver.FCMSendData;
import com.vuducminh.nicefoodserver.model.OrderModel;
import com.vuducminh.nicefoodserver.remote.IFCMServer;
import com.vuducminh.nicefoodserver.remote.RetrofitFCMClient;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;

import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 6009;
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    private ImageView img_upload;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMServer ifcmServer;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private AlertDialog dialog;

    @OnClick(R.id.fab_chat)
    void onOpenChatList() {
        startActivity(new Intent(this,ChatListActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        init();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order,R.id.nav_shipper)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView tv_user = (TextView) headerView.findViewById(R.id.tv_user);
        Common.setSpanString("Hey", Common.currentServerUser.getName(), tv_user);

        menuClick = R.id.nav_category; // Default

        checkIsOpenFromActivity();
    }

    private void init() {
        ifcmServer = RetrofitFCMClient.getInstance().create(IFCMServer.class);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        subscribeToTopic(Common.createTopicOrder());
        updatToken();

        dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Please wait...")
                .create();
    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(CommonAgr.IS_OPEN_ACTIVITY_NEW_ORDER,false);
        if(isOpenFromNewOrder) {
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;
        }
    }

    private void updatToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId().addOnFailureListener(e -> Toast.makeText(HomeActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(instanceIdResult -> {
                    Common.updateToken(HomeActivity.this,instanceIdResult.getToken(),
                            true,false);

                    Log.d("MYTOKEN",instanceIdResult.getToken());
                });
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategoryClick(CategoryClick event) {
        if (event.isSuccess()) {
            if (menuClick != R.id.nav_food_list) {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event) {
        if(event.getAction() == Common.ACTION.CREATE) {
            Toast.makeText(this, "Create Success!", Toast.LENGTH_SHORT).show();
        }
        else if(event.getAction() == Common.ACTION.UPDATE) {
            Toast.makeText(this, "Update Success!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Delete Success!", Toast.LENGTH_SHORT).show();
        }
        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event) {
        if (event.isFromFoodList()) {
            //Clear
            navController.popBackStack(R.id.nav_category, true);
            navController.navigate(R.id.nav_category);
        } else {
            //Clear
//            navController.popBackStack(R.id.nav_food_list, true);
            navController.navigate(R.id.nav_food_list);
        }
        menuClick = -1;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_category: {
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack();  // //remove all back stack
                    navController.navigate(R.id.nav_category);
                }
                break;
            }
            case R.id.nav_send_news: {
                showNewsDialog();
                break;
            }
            case R.id.nav_order: {
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_order);
                }
                break;
            }
            case R.id.nav_shipper: {
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_shipper);
                    menuClick  = R.id.nav_order;
                }
                break;
            }
            case R.id.nav_best_deals: {
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_best_deals);
                    menuClick  = R.id.nav_order;
                }
                break;
            }
            case R.id.nav_most_popular: {
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_most_popular);
                    menuClick  = R.id.nav_order;
                }
                break;
            }
            case R.id.nav_sign_out: {
                signOut();
                break;
            }
            default:
                menuClick = -1;
                break;
        }
        menuClick = menuItem.getItemId();
        return true;
    }

    private void showNewsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("News System");
        builder.setMessage("Send news notification all client");
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_news_system,null);

        EditText edt_title = (EditText)itemView.findViewById(R.id.edt_title);
        EditText edt_content = (EditText)itemView.findViewById(R.id.edt_content);
        EditText edt_link = (EditText)itemView.findViewById(R.id.edt_link);
        img_upload = (ImageView) itemView.findViewById(R.id.img_upload);

        RadioButton rdi_none = (RadioButton)itemView.findViewById(R.id.rdi_none);
        RadioButton rdi_link = (RadioButton)itemView.findViewById(R.id.rdi_link);
        RadioButton rdi_upload = (RadioButton)itemView.findViewById(R.id.rdi_image);

        rdi_none.setOnClickListener(view -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.GONE);
        });
        rdi_link.setOnClickListener(view-> {
            edt_link.setVisibility(View.VISIBLE);
            img_upload.setVisibility(View.GONE);
        });

        rdi_upload.setOnClickListener(view-> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.VISIBLE);
        });

        img_upload.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Selec Picture"),PICK_IMAGE_REQUEST);
        });

        builder.setView(itemView);
        builder.setNegativeButton("CANCLE", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton("SEND", (dialog, which) -> {
            if(rdi_none.isChecked()) {
                sendNews(edt_title.getText().toString(),edt_content.getText().toString());
            }
            else if (rdi_link.isChecked()) {
                sendNews(edt_title.getText().toString(),edt_content.getText().toString(),edt_link.getText().toString());
            }
            else if (rdi_upload.isChecked()) {
                if(imageUri != null) {
                    AlertDialog dialog1 = new AlertDialog.Builder(this).setMessage("Uploading...").create();
                    dialog1.show();

                    String file_name = UUID.randomUUID().toString();
                    StorageReference newsImages = storageReference.child("news/"+file_name);
                    newsImages.putFile(imageUri)
                            .addOnFailureListener(e -> {
                                dialog1.dismiss();
                                Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }).addOnSuccessListener(taskSnapshot -> {
                                dialog1.dismiss();
                                newsImages.getDownloadUrl().addOnSuccessListener(uri -> {
                                    sendNews(edt_title.getText().toString(),edt_content.getText().toString(),uri.toString());
                                });
                            }).addOnProgressListener(taskSnapshot -> {
                                double progress = Math.round(100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                                dialog1.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                            });
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendNews(String title, String content, String url) {
        Map<String,String> notificationData = new HashMap<String,String>();
        notificationData.put(CommonAgr.NOTI_TITLE,title);
        notificationData.put(CommonAgr.NOTI_CONTENT,content);
        notificationData.put(CommonAgr.IS_SEND_IMAGE,"true");
        notificationData.put(CommonAgr.IMAGE_URL,url);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(),notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting...").create();
        dialog.show();

        compositeDisposable.add(ifcmServer.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if(fcmResponse.getMessage_id() != 0) {
                        Toast.makeText(this,"News has been sent",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this,"News send failed",Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this,""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                }));
    }

    private void sendNews(String title, String content) {
        Map<String,String> notificationData = new HashMap<String,String>();
        notificationData.put(CommonAgr.NOTI_TITLE,title);
        notificationData.put(CommonAgr.NOTI_CONTENT,content);
        notificationData.put(CommonAgr.IS_SEND_IMAGE,"false");

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(),notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting...").create();
        dialog.show();

        compositeDisposable.add(ifcmServer.sendNotification(fcmSendData)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(fcmResponse -> {
            dialog.dismiss();
            if(fcmResponse.getMessage_id() != 0) {
                Toast.makeText(this,"News has been sent",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this,"News send failed",Toast.LENGTH_SHORT).show();
            }
        }, throwable -> {
            dialog.dismiss();
            Toast.makeText(this,""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
        }));

    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Do you really want to sign out?")
                .setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Common.selectedFood = null;
                        Common.categorySelected = null;
                        Common.currentServerUser = null;

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();
                img_upload.setImageURI(imageUri);
            }
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPrintEventListener(PrintOrderEvent event) {
        createPDFFile(event.getPath(),event.getOrderModel());
    }

    private void createPDFFile(String path, OrderModel orderModel) {
        dialog.show();

        if(new File(path).exists())
            new File(path).delete();
        try{
            Document document = new Document();
            //Save
            PdfWriter.getInstance(document,new FileOutputStream(path));
            //Open
            document.open();

            //Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("NiceFood");
            document.addCreator(Common.currentServerUser.getName());

            //Front Setting
            BaseColor colorAccent = new BaseColor(0,153,204,255);
            float fontSize = 20.0f;

            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf","UTF-8",BaseFont.EMBEDDED);
            
            //Create tiitle of Document
            Font titleFont = new Font(fontName,36.0f,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,"Order Details", Element.ALIGN_CENTER,titleFont);

            //Add more
            Font orderNumberfont = new Font(fontName,fontSize,Font.NORMAL,colorAccent);
            PDFUtils.addNewItem(document,"Order No:",Element.ALIGN_LEFT,orderNumberfont);

            Font orderNumberValueFont = new Font(fontName,20,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,orderModel.getKey(),Element.ALIGN_LEFT,orderNumberValueFont);

            PDFUtils.addLineSeperator(document);

            //Date
            PDFUtils.addNewItem(document,"Order Date",Element.ALIGN_LEFT,orderNumberfont);
            PDFUtils.addNewItem(document, new SimpleDateFormat("dd/MM/yyyy").format(orderModel.getCreateDate()),Element.ALIGN_LEFT,orderNumberValueFont);
            PDFUtils.addLineSeperator(document);
            //Account name
            PDFUtils.addNewItem(document,"Account Name",Element.ALIGN_LEFT,orderNumberfont);
            PDFUtils.addNewItem(document,orderModel.getUserName() ,Element.ALIGN_LEFT,orderNumberValueFont);
            PDFUtils.addLineSeperator(document);

            //Add product and detail
            PDFUtils.addLineSpace(document);
            PDFUtils.addNewItem(document,"Product Detail",Element.ALIGN_CENTER,titleFont);
            PDFUtils.addLineSeperator(document);

            //fetch Image from Internet
            Observable.fromIterable(orderModel.getCartItemList())
                    .flatMap(cartItem -> Common.getBitmapFromUrl(HomeActivity.this,cartItem,document))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItem ->{

                        PDFUtils.addNewItemWithLeftAndRight(document,cartItem.getFoodName(),
                                ("(0.0%)"),
                                titleFont,
                                orderNumberValueFont);
                        //Food Size and AddOn
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                "Size",
                                Common.formatSizeJsonToString(cartItem.getFoodSize()),
                                titleFont,
                                orderNumberValueFont);

                        PDFUtils.addNewItemWithLeftAndRight(document,
                                "Addon",
                                Common.formatAddonJsonToString(cartItem.getFoodAddon()),
                                titleFont,
                                orderNumberValueFont);

                        //Price
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                new StringBuilder()
                        .append(cartItem.getFoodQuantity())
                        .append("*")
                        .append(cartItem.getFoodPrice() + cartItem.getFoodExtraPrice())
                        .toString(),
                                new StringBuilder()
                        .append(cartItem.getFoodQuantity()*(cartItem.getFoodExtraPrice()+cartItem.getFoodPrice()))
                        .toString(),
                                titleFont,
                                orderNumberValueFont);

                        PDFUtils.addLineSeperator(document);

                    },throwable -> {
                        dialog.dismiss();
                        Toast.makeText(HomeActivity.this,""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                    },()->{
                        PDFUtils.addLineSeperator(document);
                        PDFUtils.addLineSeperator(document);
                        
                        PDFUtils.addNewItemWithLeftAndRight(document,"Total",
                                new StringBuilder()
                        .append(orderModel.getTotalPayment()).toString(),
                                titleFont,
                                titleFont);
                    });

            //Close
            document.close();
            dialog.dismiss();
            Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show();
            
            printPDF();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void printPDF() {
        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(this,new StringBuilder(Common.getAppPath(this))
            .append(CommonAgr.FILE_PRINT).toString());
            printManager.print("Document",printDocumentAdapter, new PrintAttributes.Builder().build());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
