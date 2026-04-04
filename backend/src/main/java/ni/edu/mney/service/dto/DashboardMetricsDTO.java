package ni.edu.mney.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DashboardMetricsDTO implements Serializable {

    private List<MetricCardDTO> cards = new ArrayList<>();
    private List<SeriesPointDTO> primarySeries = new ArrayList<>();
    private List<SeriesPointDTO> secondarySeries = new ArrayList<>();
    private List<SeriesPointDTO> tertiarySeries = new ArrayList<>();
    private List<ListItemDTO> queue = new ArrayList<>();
    private List<ActivityItemDTO> activity = new ArrayList<>();
    private ListItemDTO spotlight;

    public List<MetricCardDTO> getCards() {
        return cards;
    }

    public void setCards(List<MetricCardDTO> cards) {
        this.cards = cards;
    }

    public List<SeriesPointDTO> getPrimarySeries() {
        return primarySeries;
    }

    public void setPrimarySeries(List<SeriesPointDTO> primarySeries) {
        this.primarySeries = primarySeries;
    }

    public List<SeriesPointDTO> getSecondarySeries() {
        return secondarySeries;
    }

    public void setSecondarySeries(List<SeriesPointDTO> secondarySeries) {
        this.secondarySeries = secondarySeries;
    }

    public List<SeriesPointDTO> getTertiarySeries() {
        return tertiarySeries;
    }

    public void setTertiarySeries(List<SeriesPointDTO> tertiarySeries) {
        this.tertiarySeries = tertiarySeries;
    }

    public List<ListItemDTO> getQueue() {
        return queue;
    }

    public void setQueue(List<ListItemDTO> queue) {
        this.queue = queue;
    }

    public List<ActivityItemDTO> getActivity() {
        return activity;
    }

    public void setActivity(List<ActivityItemDTO> activity) {
        this.activity = activity;
    }

    public ListItemDTO getSpotlight() {
        return spotlight;
    }

    public void setSpotlight(ListItemDTO spotlight) {
        this.spotlight = spotlight;
    }

    public static class MetricCardDTO implements Serializable {
        private String key;
        private String label;
        private Long value;
        private String helperText;

        public MetricCardDTO() {
        }

        public MetricCardDTO(String key, String label, Long value, String helperText) {
            this.key = key;
            this.label = label;
            this.value = value;
            this.helperText = helperText;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

        public String getHelperText() {
            return helperText;
        }

        public void setHelperText(String helperText) {
            this.helperText = helperText;
        }
    }

    public static class SeriesPointDTO implements Serializable {
        private String label;
        private Long value;
        private Long secondaryValue;
        private Long tertiaryValue;

        public SeriesPointDTO() {
        }

        public SeriesPointDTO(String label, Long value) {
            this.label = label;
            this.value = value;
        }

        public SeriesPointDTO(String label, Long value, Long secondaryValue, Long tertiaryValue) {
            this.label = label;
            this.value = value;
            this.secondaryValue = secondaryValue;
            this.tertiaryValue = tertiaryValue;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

        public Long getSecondaryValue() {
            return secondaryValue;
        }

        public void setSecondaryValue(Long secondaryValue) {
            this.secondaryValue = secondaryValue;
        }

        public Long getTertiaryValue() {
            return tertiaryValue;
        }

        public void setTertiaryValue(Long tertiaryValue) {
            this.tertiaryValue = tertiaryValue;
        }
    }

    public static class ListItemDTO implements Serializable {
        private Long id;
        private String title;
        private String subtitle;
        private String status;
        private String timestamp;
        private String meta;

        public ListItemDTO() {
        }

        public ListItemDTO(Long id, String title, String subtitle, String status, String timestamp, String meta) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.status = status;
            this.timestamp = timestamp;
            this.meta = meta;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getMeta() {
            return meta;
        }

        public void setMeta(String meta) {
            this.meta = meta;
        }
    }

    public static class ActivityItemDTO implements Serializable {
        private Long id;
        private String title;
        private String subtitle;
        private String timestamp;
        private String action;

        public ActivityItemDTO() {
        }

        public ActivityItemDTO(Long id, String title, String subtitle, String timestamp, String action) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.timestamp = timestamp;
            this.action = action;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}