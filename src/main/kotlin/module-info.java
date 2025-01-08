module dev.namexdd.machinevision_hw {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires opencv;
    requires java.desktop;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    exports dev.namexdd.machinevision_hw;
    exports dev.namexdd.machinevision_hw.colors_thresholding;
    exports dev.namexdd.machinevision_hw.barcode_reader;
    exports dev.namexdd.machinevision_hw.multiple_objects;

    opens dev.namexdd.machinevision_hw to javafx.fxml;
    opens dev.namexdd.machinevision_hw.colors_thresholding to javafx.fxml;
    opens dev.namexdd.machinevision_hw.barcode_reader to javafx.fxml;
    opens dev.namexdd.machinevision_hw.multiple_objects to javafx.fxml;
}
