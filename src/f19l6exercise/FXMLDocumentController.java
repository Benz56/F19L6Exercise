/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package f19l6exercise;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Benjamin Staugaard | Benz56
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label resultLabel;
    @FXML
    private ImageView image1, image2, image3;
    @FXML
    private Button stop1Btn, stop2Btn, stop3Btn, startBtn;

    private Image[] images;
    private int sequences;
    private ScheduledFuture<?> view1, view2, view3;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        images = IntStream.range(0, 11).mapToObj(val -> new Image(new File("src\\f19l6exercise\\images\\fruits" + val + ".png").toURI().toString())).toArray(Image[]::new);
        int[] progress = {ThreadLocalRandom.current().nextInt(10), ThreadLocalRandom.current().nextInt(10), ThreadLocalRandom.current().nextInt(10)};
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        startBtn.setOnAction(event -> {
            toggleButtons(false);
            sequences = 0;
            resultLabel.setText("Running...");
            view1 = executor.scheduleAtFixedRate(() -> progress[0] = setImage(image1, progress[0]), 0, 100, TimeUnit.MILLISECONDS);
            view2 = executor.scheduleAtFixedRate(() -> progress[1] = setImage(image2, progress[1]), 0, 120, TimeUnit.MILLISECONDS);
            view3 = executor.scheduleAtFixedRate(() -> progress[2] = setImage(image3, progress[2]), 0, 140, TimeUnit.MILLISECONDS);
            startBtn.setDisable(true);
        });
        stop1Btn.setOnAction(event -> anyStopPressed(view1, stop1Btn));
        stop2Btn.setOnAction(event -> anyStopPressed(view2, stop2Btn));
        stop3Btn.setOnAction(event -> anyStopPressed(view3, stop3Btn));
    }

    private synchronized void anyStopPressed(ScheduledFuture<?> view, Button button) {
        view.cancel(true);
        button.setDisable(true);
        sequences++;
        if (sequences == 3) {
            long maxEqualSlots = Stream.of(image1.getImage(), image2.getImage(), image3.getImage()).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).values().stream().sorted((o1, o2) -> Long.compare(o2, o1)).findFirst().orElse(1L);
            resultLabel.setText(maxEqualSlots == 3 ? "3 ens: Jackpot" : maxEqualSlots == 2 ? "2 ens: Du vinder lidt" : "TABER!");
            toggleButtons(true);
        }
    }

    private void toggleButtons(boolean invert) {
        startBtn.setDisable(!invert);
        stop1Btn.setDisable(invert);
        stop2Btn.setDisable(invert);
        stop3Btn.setDisable(invert);
    }

    private int setImage(ImageView view, int progress) {
        Platform.runLater(() -> view.setImage(images[progress]));
        return progress == 9 ? 0 : progress + 1; // Return the new progress value.
    }
}
