package com.ozgen.binancebot.model.binance;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

//@Entity
//@Table(name = "snapshot_data")
//@Data
//@ToString
public class SnapshotData {

//    @Id
//    @GeneratedValue(generator = "UUID")
//    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private int code;
    private String msg;

//    @OneToMany(mappedBy = "snapshotData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SnapshotVo> snapshotVos;


    public Double getCoinValue(String assetName) {
        Double result = 0.0;
//        if (this == null || this.getSnapshotVos() == null) {
//            return result;
//        }
//
//        SnapshotVo lastSnapShotVo = this.snapshotVos.get(this.snapshotVos.size() - 1);
//        SnapshotData.Data data = lastSnapShotVo.getData();
//        if (data != null && data.getBalances() != null) {
//            for (SnapshotData.Balance balance : data.getBalances()) {
//                if (assetName.equals(balance.getAsset())) {
//                    try {
//                        result += Double.parseDouble(balance.getFree());
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace();
//                        // Handle parse error
//                    }
//                }
//            }
//        }

        return result;
    }

//    @Entity
//    @Table(name = "snapshot_vo")
//    @lombok.Data
//    @ToString
    public static class SnapshotVo {

//        @Id
//        @GeneratedValue(generator = "UUID")
//        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        private String id;

        private String type;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date updateTime;

//        @ManyToOne
//        @JoinColumn(name = "snapshot_data_id")
        private SnapshotData snapshotData;

//        @OneToOne(mappedBy = "snapshotVo", cascade = CascadeType.ALL, orphanRemoval = true)
        private Data data;

    }

//    @Entity
//    @Table(name = "data")
//    @lombok.Data
//    @ToString
    public static class Data {
//        @Id
//        @GeneratedValue(generator = "UUID")
//        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        private String id;

        private String totalAssetOfBtc;

//        @OneToOne
//        @JoinColumn(name = "snapshot_vo_id")
        private SnapshotVo snapshotVo;

//        @OneToMany(mappedBy = "data", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Balance> balances;
    }

//    @Entity
//    @Table(name = "balance")
//    @lombok.Data
//    @ToString
    public static class Balance {
//        @Id
//        @GeneratedValue(generator = "UUID")
//        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        private String id;

        private String asset;
        private String free;
        private String locked;

//        @ManyToOne
//        @JoinColumn(name = "data_id")
        private Data data;

    }
}

