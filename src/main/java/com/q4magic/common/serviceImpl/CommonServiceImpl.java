package com.q4magic.common.serviceImpl;

import com.q4magic.common.service.CommonService;
import jakarta.mail.internet.MimeMessage;
import org.aspectj.util.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service(value = "commonService")
public class CommonServiceImpl implements CommonService {
    @Value("${360pipeDrive}")
    String FILE_DIRECTORY;

    @Value("${imageContextPath}")
    String imageContextPath;

    @Value("${spring.mail.properties.mail.from}")
    String mailFrom;

    @Value("${spring.mail.password}")
    String mailPassword;

    @Autowired
    private JavaMailSender mailSender;

    private static final DateTimeFormatter OUTPUT_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US);

    /**
     * Accepts LocalDate from DB and formats using ZonedDateTime
     */
    public static String formatDueLongZoned(LocalDate dueDate, ZoneId zoneId) {
        if (dueDate == null) {
            return "TBD";
        }

        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }

        ZonedDateTime zonedDateTime = dueDate.atStartOfDay(zoneId);
        return zonedDateTime.format(OUTPUT_FORMATTER);
    }

    /**
     * If dueDate is stored as String like "2026-02-15"
     */
    @Override
    public String formatDueLongZoned(String dateString, ZoneId zoneId) {

        if (dateString == null || dateString.isBlank()) {
            return "TBD";
        }

        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }

        try {
            // Match your convertDateToString format
            DateTimeFormatter inputFormatter =
                    DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm:ss a", Locale.ENGLISH);

            LocalDateTime localDateTime = LocalDateTime.parse(dateString, inputFormatter);

            ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

            return zonedDateTime.format(OUTPUT_FORMATTER);

        } catch (Exception e) {
            return dateString; // fallback like JS version
        }
    }


    @Override
    public Date convertStringToDate(String dateStr) {
        try {
            SimpleDateFormat formatter;

            if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                // MM/dd/yyyy
                formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);

            } else if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}$")) {
                // yyyy-MM-dd
                formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            } else if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z")) {
                // ISO 8601 with Z (UTC)
                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            } else if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}T.*[+-]\\d{4}")) {
                // ISO 8601 with +0000
                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);

            } else {
                // MM/dd/yyyy, hh:mm:ss a
                formatter = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a", Locale.ENGLISH);
            }

            formatter.setLenient(false);
            return formatter.parse(dateStr);

        } catch (ParseException e) {
            throw new RuntimeException(
                    "Error converting date: " + dateStr + " - " + e.getMessage()
            );
        }
    }

    @Override
    public String convertDateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    @Override
    public Map<String, Object> uploadFiles(MultipartFile[] files, Integer loginUserId, String folderName) {
        Map<String, Object> resBody = new HashMap<>();
        List<Map<String, String>> uploadedFiles = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String originalFilename = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]+", "_");

                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

                boolean isImage = fileExtension.matches("jpg|jpeg|png");
                boolean isDocument = fileExtension.matches("pdf|doc|docx|xls|xlsx|html");

                if (!(isImage || isDocument)) {
                    resBody.put("status", 400);
                    resBody.put("message", "." + fileExtension + " file type not supported.");
                    return resBody;
                }

                // Generate dynamic directory path
                String dynamicPath = loginUserId + "/" + "tempImage/" + folderName + "/";

                // Create directory if it doesn't exist
                File targetDirectory = new File(FILE_DIRECTORY + dynamicPath);
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs();
                }

                String fullPath = FILE_DIRECTORY + dynamicPath + originalFilename;
                File targetFile = new File(fullPath);

                // Save the file
                file.transferTo(targetFile);

                //✅ Set file permissions to 755 (rwxr - xr - x)
                setFilePermissions(targetFile);

                // Add file info to response list
                String fileUrl = imageContextPath + dynamicPath.replace("\\", "/") + originalFilename; // Convert path to URL
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("imageName", originalFilename);
                fileInfo.put("imageURL", fileUrl);
                fileInfo.put("fileType", isImage ? "image" : "document");
                uploadedFiles.add(fileInfo);
            }

            resBody.put("uploadedFiles", uploadedFiles);
            resBody.put("status", 200);
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("status", "error");
            resBody.put("message", e.getMessage());
        }

        return resBody;
    }

    @Override
    public String updateFileLocation(String image, Integer loginUserId, String tempFolderName, String newFolderName) {
        if (image == null || image.trim().isEmpty()) return image;

        // If image is already a final url/path (not temp), DO NOT move it
        // e.g. http://localhost/360pipe/usercontent/196/todo/7/55/uuid.jpg
        // or anything that doesn't contain "/tempImage/"
        if (!image.contains("/tempImage/") && !image.contains("\\tempImage\\")) {
            return image; // ✅ old file => keep as-is
        }

        // Extract filename from URL/path
        String normalized = image.replace("\\", "/");
        String[] arr = normalized.split("/");
        String originalFileName = arr[arr.length - 1];

        File tempImageDirectory = new File(FILE_DIRECTORY + loginUserId + "/tempImage/" + tempFolderName);
        File destinationDirectory = new File(FILE_DIRECTORY + loginUserId + "/" + newFolderName);

        System.out.println("================= tempImageDirectory ================" + tempImageDirectory);
        System.out.println("================= destinationDirectory ================" + destinationDirectory);

        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        // Always resolve the source file properly
        File sourceFile = new File(tempImageDirectory, originalFileName);

        // ✅ If the source file is not present in temp, don't fail the update — just keep old image path
        if (!sourceFile.exists()) {
            System.err.println("Source file not found in temp, skipping move: " + sourceFile.getAbsolutePath());
            return image; // ✅ keep existing path
        }

        File destinationFile = new File(destinationDirectory, originalFileName);

        try {
            FileUtil.copyFile(sourceFile, destinationFile);
            setFilePermissions(destinationFile);

            String imageDynamicPath = loginUserId + "/" + newFolderName + "/" + originalFileName;

            // cleanup (optional)
            sourceFile.delete();

            return imageContextPath + imageDynamicPath;
        } catch (IOException e) {
            throw new RuntimeException("File move error: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDirectoryRecursively(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file); // Recursively delete sub-files/sub-directories
                }
            }
        }
        if (directory.delete()) {
            System.out.println("Deleted: " + directory.getAbsolutePath());
        } else {
            System.out.println("Failed to delete: " + directory.getAbsolutePath());
        }
    }

    private static void setFilePermissions(File file) throws IOException {
        // Use PosixFilePermissions for Unix-based systems
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            Files.setPosixFilePermissions(file.toPath(), perms);
        }
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String body, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml); // Second param 'true' enables HTML

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            // Log properly so you actually see the error
            System.err.println("Mail Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendEmailWithAttachment(String toEmail, String subject, String body, boolean isHtml,String filename, String filepath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml); // Second param 'true' enables HTML
            if (filename != null && filepath != null) {
                FileSystemResource file = new FileSystemResource(new File(filepath));
                helper.addAttachment(filename, file);
            }
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            // Log properly so you actually see the error
            System.err.println("Mail Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getDomainMX(String input) {
        // 1) Accept email or domain; extract domain if needed
        if (input == null || input.isBlank()) return List.of();
        String domain = input.trim().toLowerCase();
        int at = domain.indexOf('@');
        if (at >= 0) domain = domain.substring(at + 1); // extract after '@'

        // Very basic domain sanity check
        if (!domain.matches("^[a-z0-9.-]+\\.[a-z]{2,}$")) return List.of();

        // 2) JNDI DNS env with timeouts
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put(Context.PROVIDER_URL, "dns:");
        env.put("com.sun.jndi.dns.timeout.initial", "2000");
        env.put("com.sun.jndi.dns.timeout.retries", "2");

        Set<String> result = new LinkedHashSet<>();
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            Attribute mxAttr = (attrs != null) ? attrs.get("MX") : null;

            if (mxAttr == null || mxAttr.size() == 0) {
                // No MX -> RFC fallback: try direct A/AAAA of domain
                result.add(domain);
            } else {
                // Collect and sort by priority
                List<int[]> priorities = new ArrayList<>(); // index->priority
                List<String> hosts = new ArrayList<>();
                for (int i = 0; i < mxAttr.size(); i++) {
                    String rec = String.valueOf(mxAttr.get(i)).trim(); // e.g. "10 alt1.aspmx.l.google.com."
                    String[] parts = rec.split("\\s+");
                    int pref = (parts.length >= 2) ? safeParseInt(parts[0], 0) : 0;
                    String host = (parts.length >= 2) ? parts[1] : parts[0];
                    if (host.endsWith(".")) host = host.substring(0, host.length() - 1);
                    hosts.add(host.toLowerCase());
                    priorities.add(new int[]{pref, hosts.size() - 1});
                }
                priorities.sort(Comparator.comparingInt(a -> a[0]));
                for (int[] p : priorities) result.add(hosts.get(p[1]));
            }
        } catch (NamingException ne) {
            // Distinguish NXDOMAIN vs other errors when possible
            String msg = ne.getMessage() == null ? "" : ne.getMessage().toLowerCase();
            if (msg.contains("response code 3") || msg.contains("name not found") || msg.contains("nxdomain")) {
                // Domain truly doesn't resolve in DNS -> return empty to signal "invalid domain"
                return List.of();
            }
            // For transient errors, conservative fallback
            result.add(domain);
        } finally {
            if (ctx != null) try {
                ctx.close();
            } catch (Exception ignore) {
            }
        }
        return new ArrayList<>(result);
    }

    @Override
    public String dbToDdTimeZone(String startDate, String timeZone) throws ParseException {
        if (timeZone == null || timeZone.trim().isEmpty()) {
            // Decide fallback: return as-is, use UTC, or throw a meaningful exception
            return startDate;   // or return startDate unchanged
            // Alternatively: timeZone = "UTC";
        }
        DateFormat formatterUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatterUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = formatterUTC.parse(startDate);

        DateFormat formatterTarget = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatterTarget.setTimeZone(TimeZone.getTimeZone(timeZone));
        return formatterTarget.format(date);
    }

    @Override
    public String dbDateTime(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(date);
        SimpleDateFormat dbt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dbt.format(modifiedDate);
    }

    @Override
    public Date convertDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.parse(date);
    }

    @Override
    public String convertEventTimeZoneToUserDB(String date, String fromTimeZone, String toTimeZone) throws ParseException {
        if (fromTimeZone == null || fromTimeZone.equals(toTimeZone) || toTimeZone == null) {
            return date;
        } else {
            ZoneId fTimeZone = ZoneId.of(fromTimeZone);
            ZoneId tTimeZone = ZoneId.of(toTimeZone);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(date, dtf);
            ZonedDateTime fTime = ZonedDateTime.of(ldt, fTimeZone);
            ZonedDateTime tTime = fTime.withZoneSameInstant(tTimeZone);
            DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return dtf2.format(tTime);
        }
    }

    @Override
    public String convertDateTimeToTimeZone(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(date);
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return dt.format(modifiedDate);
    }

    @Override
    public String dbDate(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
        SimpleDateFormat dbt = new SimpleDateFormat("yyyy-MM-dd");
        return dbt.format(modifiedDate);
    }

    @Override
    public String dateObjectToDbDateTime(Date date) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dt.format(date);
    }

    @Override
    public LocalDateTime covertLocalDateTime(String date) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(date, formatter);
    }

    @Override
    public Map<String, Integer> getSplitDate(String sourcedate) throws ParseException {
        Map<String, Integer> resBody = new HashMap<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df.parse(sourcedate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
//            resBody.put("week", cal.get(Calendar.WEEK_OF_MONTH));
        int d = Integer.parseInt(new SimpleDateFormat("d", Locale.ENGLISH).format(date));
        resBody.put("week", (int) Math.ceil((double) d / 7));

        resBody.put("day", cal.get(Calendar.DAY_OF_WEEK));
        Integer month = Integer.valueOf(new SimpleDateFormat("M", Locale.ENGLISH).format(date));
        resBody.put("month", month);
        Integer year = Integer.valueOf(new SimpleDateFormat("yyyy", Locale.ENGLISH).format(date));
        resBody.put("year", year);
        return resBody;
    }

    @Override
    public String convertDateTimeObjectToDBDateTime(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(date);
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dt.format(modifiedDate);
    }

    @Override
    public LocalDateTime changeDate(LocalDateTime eventDate, int day) {
        int month = eventDate.getMonthValue();
        if (day > 28) {
            if (month == 2) {
                int year = eventDate.getYear();
                boolean isLeapYear = (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
                int daysInFebruary = isLeapYear ? 29 : 28;
                eventDate = eventDate.withDayOfMonth(daysInFebruary);
            } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                if (day == 31) {
                    eventDate = eventDate.withDayOfMonth(30);
                } else {
                    eventDate = eventDate.withDayOfMonth(day);
                }
            } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                eventDate = eventDate.withDayOfMonth(day);
            }
        }
        return eventDate;
    }

    @Override
    public String getDateTime(int year, Month month, int day, int week) {
        DayOfWeek dayOfWeek = null;
        if (day == 1) {
            dayOfWeek = DayOfWeek.valueOf("SUNDAY");
        } else if (day == 2) {
            dayOfWeek = DayOfWeek.valueOf("MONDAY");
        } else if (day == 3) {
            dayOfWeek = DayOfWeek.valueOf("TUESDAY");
        } else if (day == 4) {
            dayOfWeek = DayOfWeek.valueOf("WEDNESDAY");
        } else if (day == 5) {
            dayOfWeek = DayOfWeek.valueOf("THURSDAY");
        } else if (day == 6) {
            dayOfWeek = DayOfWeek.valueOf("FRIDAY");
        } else if (day == 7) {
            dayOfWeek = DayOfWeek.valueOf("SATURDAY");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate firstOccurrence = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        LocalDate nthOccurrence = firstOccurrence.plusWeeks(week - 1);
        return nthOccurrence.toString();
    }

    @Override
    public String getLastWeek(int year, Month month, int day) throws ParseException {
        DayOfWeek dayOfWeek = null;
        if (day == 1) {
            dayOfWeek = DayOfWeek.valueOf("SUNDAY");
        } else if (day == 2) {
            dayOfWeek = DayOfWeek.valueOf("MONDAY");
        } else if (day == 3) {
            dayOfWeek = DayOfWeek.valueOf("TUESDAY");
        } else if (day == 4) {
            dayOfWeek = DayOfWeek.valueOf("WEDNESDAY");
        } else if (day == 5) {
            dayOfWeek = DayOfWeek.valueOf("THURSDAY");
        } else if (day == 6) {
            dayOfWeek = DayOfWeek.valueOf("FRIDAY");
        } else if (day == 7) {
            dayOfWeek = DayOfWeek.valueOf("SATURDAY");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        LocalDate lastSunday = lastDayOfMonth.with(TemporalAdjusters.previousOrSame(dayOfWeek));
        return lastSunday.toString();
    }

    @Override
    public String convertEventTimeZoneToUser(String date, String fromTimeZone, String toTimeZone) throws ParseException {
        if (fromTimeZone == null || fromTimeZone.equals(toTimeZone) || toTimeZone == null) {
            return date;
        } else {
            ZoneId fTimeZone = ZoneId.of(fromTimeZone);
            ZoneId tTimeZone = ZoneId.of(toTimeZone);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(date, dtf);
            ZonedDateTime fTime = ZonedDateTime.of(ldt, fTimeZone);
            ZonedDateTime tTime = fTime.withZoneSameInstant(tTimeZone);
            DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
            return dtf2.format(tTime);
        }
    }

    @Override
    public String dateObjectToDisplayDate(Date date) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return dt.format(date);
    }

    @Override
    public int convertDateTimeObjectToReturnYear(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(date);
        SimpleDateFormat dt = new SimpleDateFormat("yyyy");
        return Integer.parseInt(dt.format(modifiedDate));
    }

    @Override
    public int convertDateTimeObjectToReturnMonth(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(date);
        SimpleDateFormat dt = new SimpleDateFormat("MM");
        return Integer.parseInt(dt.format(modifiedDate));
    }

    @Override
    public Map<String, Integer> getSplitDateOnlyWeek(String sourcedate) throws ParseException {
        Map<String, Integer> resBody = new HashMap<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df.parse(sourcedate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        resBody.put("week", cal.get(Calendar.WEEK_OF_MONTH));
        resBody.put("day", cal.get(Calendar.DAY_OF_WEEK));
        Integer month = Integer.valueOf(new SimpleDateFormat("M", Locale.ENGLISH).format(date));
        resBody.put("month", month);
        Integer year = Integer.valueOf(new SimpleDateFormat("yyyy", Locale.ENGLISH).format(date));
        resBody.put("year", year);
        return resBody;
    }

    @Override
    public String getDate(String day, int week, int month, int year) throws ParseException {
        Map<String, String> resBody = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.WEEK_OF_MONTH, week);
        cal.set(Calendar.MONTH, (month - 1));
        cal.set(Calendar.YEAR, year);
        if (day.equals("Sunday")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        } else if (day.equals("Monday")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else if (day.equals("Tuesday")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        } else if (day.equals("Wednesday")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        } else if (day.equals("Thursday")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        } else if (day.equals("Friday")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        } else if (day.equals("Saturday")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        }
        return sdf.format(cal.getTime());
    }

    @Override
    public int dateCampare(String dateOne, String dateTwo) throws ParseException {
        Map<String, String> resBody = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = sdf.parse(dateOne);
        Date date2 = sdf.parse(dateTwo);
        int result = date1.compareTo(date2);
        return result;
    }

    @Override
    public String dateObjectToDbDate(Date date) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        return dt.format(date);
    }

    @Override
    public Date addOneDays(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, hours);
        return cal.getTime();
    }

    @Override
    public Date convertDateOnly(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(date);
    }

    @Override
    public String displayDateTime(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        SimpleDateFormat dt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return dt.format(modifiedDate);
    }

    @Override
    public String convertTimeZoneToDbDate(String date) throws ParseException {
        Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date);
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dt.format(modifiedDate);
    }

    @Override
    public String changeTimeZoneName(String name) {
        if (name.equals("India Standard Time")) {
            name = "Asia/Kolkata";
        } else if (name.equals("Mountain Standard Time")) {
            name = "America/Denver";
        }
        return name;
    }

    @Override
    public String nl2br(String text) {
        return text.replace("\n", "<br />");
    }

    private static int safeParseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static final class MxEntry {
        final int pref;
        final String host;

        MxEntry(int pref, String host) {
            this.pref = pref;
            this.host = host;
        }
    }

    @Override
    public String dbDateToDisplayDateTime(String date) throws ParseException {
        try {
            Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
            SimpleDateFormat dbt = new SimpleDateFormat("hh:mma - EEEE, dd MMMM yyyy");
            return dbt.format(modifiedDate);
        } catch (ParseException exception) {
            return "";
        }
    }

    @Override
    public String dayName(String date) throws ParseException {
        try {
            Date modifiedDate = new SimpleDateFormat("MM/dd/yyyy").parse(date);
            SimpleDateFormat dbt = new SimpleDateFormat("EEEE");
            return dbt.format(modifiedDate);
        } catch (ParseException exception) {
            exception.printStackTrace();
            return "";
        }
    }

    @Override
    public String lastCharaters(String input, int no) {
        String lastCharaters;
        if (input.length() > no) {
            lastCharaters = input.substring(input.length() - no);
        } else {
            lastCharaters = input;
        }
        return lastCharaters;
    }

    @Override
    public String remainingTime(int minutes, String firstTime, String secondTime) throws ParseException {
        String format = "HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date dateObj1 = sdf.parse(firstTime);
        Date dateObj2 = sdf.parse(secondTime);
        long dif = dateObj1.getTime();
        String lastSlot = firstTime;
        while (dif < dateObj2.getTime()) {
            Date slot = new Date(dif);
            lastSlot = dateTimeZoneToTime(slot.toString());
            dif += minutes*60000;
        }

        Date date1 = sdf.parse(lastSlot);
        Date date2 = sdf.parse("24:00:00");
        long difference = date2.getTime() - date1.getTime();
        difference = difference/60000;
        long addMinutes = 0;
        if(difference > minutes) {
            addMinutes = difference-minutes;
        } else {
            addMinutes = minutes-difference;
        }

        Date d = sdf.parse("00:00:00");
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, (int) addMinutes);
        String newTime = sdf.format(cal.getTime());
        return newTime;
    }

    public static String dateTimeZoneToTime(String date) throws ParseException {
        try {
            Date modifiedDate = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").parse(date);
            SimpleDateFormat dbt = new SimpleDateFormat("HH:mm:ss");
            return dbt.format(modifiedDate);
        } catch (ParseException exception) {
            return "";
        }
    }

    @Override
    public List<String> slotList(int minutes, String firstTime, String secondTime) throws ParseException {
        List<String> slotList = new ArrayList<>();
//        String firstTime = "00:00:00";
//        String secondTime = "24:00:00";
        String format = "HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date dateObj1 = sdf.parse(firstTime);
        Date dateObj2 = sdf.parse(secondTime);
        long dif = dateObj1.getTime();
        String lastSlot = firstTime;
        while (dif < dateObj2.getTime()) {
            Date slot = new Date(dif);
            slotList.add(dateTimeZoneToTime(slot.toString()));
            lastSlot = dateTimeZoneToTime(slot.toString());
            dif += minutes*60000;
        }
        Date slot = new Date(dif);
        if(secondTime.equals("24:00:00")) {
            if(!dateTimeZoneToTime(slot.toString()).equals("00:00:00")) {
                slotList.remove(lastSlot);
            }
        } else {
            if(!dateTimeZoneToTime(slot.toString()).equals(secondTime)) {
                slotList.remove(lastSlot);
            }
        }
        return slotList;
    }

    @Override
    public String currentTime(String timeZone) throws ParseException {
        DateFormat formatterIST = new SimpleDateFormat("HH:mm:ss");
        formatterIST.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        DateFormat formatterUTC = new SimpleDateFormat("HH:mm:ss");
        formatterUTC.setTimeZone(TimeZone.getTimeZone(timeZone));
        return formatterUTC.format(date);
    }

    @Override
    public boolean checkBigFirstTime(String firstTime, String secondTime) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss") ;
        if(dateFormat.parse(firstTime).after(dateFormat.parse(secondTime))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String displayDbDateTimeToTime(String date) throws ParseException {
        try {
            Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
            SimpleDateFormat dbt = new SimpleDateFormat("HH:mm:ss");
            return dbt.format(modifiedDate);
        } catch (ParseException exception){
            exception.printStackTrace();
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public List<String> removeBookSlot(int minutes, List<String> slotList, String startTime, String endTime) {
        List<String> freeSlotList = new ArrayList<>();
        try {
            if(slotList.contains(startTime)) {
                slotList.remove(startTime);
            } else {
                Long count = slotList.stream().count();
                for(int i = 0; i < count; i++) {
                    if(i != count-1) {
                        Date time1 = new SimpleDateFormat("HH:mm:ss").parse(slotList.get(i));
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(time1);
                        calendar1.add(Calendar.DATE, 1);

                        Date time2 = new SimpleDateFormat("HH:mm:ss").parse(slotList.get(i+1));
                        Calendar calendar2 = Calendar.getInstance();
                        calendar2.setTime(time2);
                        calendar2.add(Calendar.DATE, 1);

                        Date d = new SimpleDateFormat("HH:mm:ss").parse(startTime);
                        Calendar calendar3 = Calendar.getInstance();
                        calendar3.setTime(d);
                        calendar3.add(Calendar.DATE, 1);
                        Date x = calendar3.getTime();
                        if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                            slotList.remove(slotList.get(i));
                            break;
                        }
                    }
                }
            }

            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(startTime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);
            calendar1.add(Calendar.DATE, 1);

            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(endTime);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
            calendar2.add(Calendar.DATE, 1);

            for (String slotTime:slotList) {
                Date d = new SimpleDateFormat("HH:mm:ss").parse(slotTime);
                Calendar calendar3 = Calendar.getInstance();
                calendar3.setTime(d);
                calendar3.add(Calendar.DATE, 1);
                Date x = calendar3.getTime();
                if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                } else {
                    freeSlotList.add(slotTime);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return freeSlotList;
    }

    @Override
    public String convertDateTimeToTime(String date) throws ParseException {
        try {
            Date modifiedDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(date);
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss");
            return dt.format(modifiedDate);
        } catch (ParseException exception) {
            throw new ParseException(exception.getMessage(),0);
        }
    }

    @Override
    public String br2nl(String html) {
        Document document = Jsoup.parse(html);
        document.select("br").append("\\n");
        return document.text().replace("\\n", "\n");
    }

    public static String ucFirst(String str) {
        str = str.trim();
        if(str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public String ucWords(String str) {
        if(str == null) {
            return str;
        } else {
            String[] s = str.split(" ");
            str = "";
            for(int i = 0; i < s.length; i++) {
                if(s[i].length() > 0) {
                    if(!str.equals("")) {
                        str += " ";
                    }
                    str += ucFirst(s[i]);
                }
            }
        }
        return str;
    }
}
