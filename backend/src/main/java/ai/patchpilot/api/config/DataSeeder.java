package ai.patchpilot.api.config;

import ai.patchpilot.api.model.KnowledgeBaseArticle;
import ai.patchpilot.api.model.KnowledgeBaseArticle.Category;
import ai.patchpilot.api.repository.KnowledgeBaseArticleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final KnowledgeBaseArticleRepository repo;

    public DataSeeder(KnowledgeBaseArticleRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        repo.saveAll(seedArticles());
    }

    private List<KnowledgeBaseArticle> seedArticles() {
        return List.of(

            KnowledgeBaseArticle.builder()
                .title("MSI Error 1603 — Fatal Error During Installation")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern("1603,fatal error,installation failed,msi,windows installer")
                .category(Category.CORRUPTION)
                .rootCause("""
                    Error 1603 is a catch-all Windows Installer exit code meaning a fatal error \
                    occurred during installation, and the operation was rolled back. It most commonly \
                    surfaces when a locked file prevents the MSI from writing, when a previous \
                    installation of the same product left behind corrupt registry entries or residual \
                    files, or when the Windows Installer service itself is in a degraded state. \
                    Elevated permissions, active antivirus scanning, or a full %TEMP% directory can \
                    also trigger this code. Because 1603 is a wrapper around dozens of underlying \
                    conditions, the MSI verbose log (msiexec /l*v) is the authoritative source for \
                    the real cause.""")
                .resolutionSteps("""
                    1. **Generate a verbose log** — Re-run the installer from an elevated command prompt with logging enabled:
                       ```
                       msiexec /i "installer.msi" /l*v "%TEMP%\\install_log.txt"
                       ```
                       Open the log and search for `Return value 3` — the lines above it identify the root action that failed.

                    2. **Clean the %TEMP% directory** — Open `%TEMP%` in Explorer, select all, and delete. Retry the installer.

                    3. **Reboot into a clean state** — Open Task Manager and end non-essential processes. Temporarily disable antivirus real-time protection. Retry the installer as Administrator.

                    4. **Remove residual registry keys** — Open `regedit` and navigate to:
                       `HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall`
                       Delete any stale key referencing the product. Do the same under `HKEY_CURRENT_USER\\SOFTWARE`.

                    5. **Re-register Windows Installer** — Run from an elevated prompt:
                       ```
                       msiexec /unregister
                       msiexec /regserver
                       ```
                       Then retry the installation.""")
                .build(),

            KnowledgeBaseArticle.builder()
                .title("Missing Visual C++ Redistributable")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern("vcruntime,msvcp,msvcr,vcredist,api-ms-win,runtime dll,c++ redistributable,0xc000007b")
                .category(Category.DEPENDENCY)
                .rootCause("""
                    Many Windows applications ship without bundling the Visual C++ runtime DLLs \
                    they depend on, expecting them to be pre-installed on the target machine via the \
                    Visual C++ Redistributable packages. When a required version (e.g., 2015–2022 x64) \
                    is absent, partially corrupt, or mismatched in bitness (32-bit app trying to load \
                    64-bit runtime), Windows cannot resolve the DLL and the application fails to \
                    launch or install with an error such as "VCRUNTIME140.dll was not found" or \
                    0xc000007b. Side-by-side (WinSxS) assembly corruption can cause the same \
                    symptoms even when the redistributable appears to be installed.""")
                .resolutionSteps("""
                    1. **Identify the required version** — Check the application's system requirements page or installer log for the specific VC++ year (2010, 2013, 2015–2022) and architecture (x86/x64).

                    2. **Download directly from Microsoft** — Visit the official Microsoft Visual C++ Redistributable downloads page and install both the x86 and x64 variants for the required year, even on a 64-bit OS (many apps use 32-bit components).

                    3. **Repair existing installations** — In Settings → Apps, search for "Visual C++", select each relevant entry, and choose **Modify → Repair**.

                    4. **Run the all-in-one redistributable pack** — Tools like `VisualCppRedist_AIO` install every VC++ runtime from 2005 to 2022 in one step — useful when the specific version is unclear.

                    5. **Re-run the application installer** — After installing the redistributables, reboot and retry the installation or application launch.""")
                .build(),

            KnowledgeBaseArticle.builder()
                .title("Antivirus Blocking Installer Execution")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern("antivirus,blocked,quarantined,access denied,threat,defender,real-time protection,setup.exe blocked")
                .category(Category.ANTIVIRUS)
                .rootCause("""
                    Security software (Windows Defender, third-party AV, or endpoint EDR agents) \
                    uses heuristic and behavioural analysis to flag unknown or newly published \
                    installers as potentially malicious — a phenomenon known as a false positive. \
                    Installers are high-risk targets for heuristic engines because they perform \
                    privilege escalation, write to protected directories, modify registry hives, \
                    and inject DLLs — all behaviours indistinguishable from malware without a \
                    trusted signature. Corporate environments running EDR products (CrowdStrike \
                    Falcon, SentinelOne, etc.) are particularly prone to blocking legitimate \
                    installers that lack a well-known vendor code-signing certificate.""")
                .resolutionSteps("""
                    1. **Check quarantine history** — Open Windows Security → Virus & threat protection → Protection history. Look for any blocked or quarantined item matching the installer name and restore it if a false positive is confirmed.

                    2. **Verify the installer's authenticity** — Right-click the installer → Properties → Digital Signatures tab. Confirm the signature is valid and from the expected vendor. Do not proceed if the signature is missing or invalid.

                    3. **Temporarily disable real-time protection** — Windows Security → Virus & threat protection → Manage settings → toggle Real-time protection OFF. Run the installer immediately, then re-enable protection.
                       > ⚠️ Only do this for software from a trusted, verified source.

                    4. **Add an exclusion** — If the product requires a persistent exclusion:
                       Windows Security → Virus & threat protection → Manage settings → Exclusions → Add an exclusion → select the installer file and the destination install folder.

                    5. **Escalate to IT/security team** — In managed environments, the AV policy may prevent exclusions. Submit the installer hash to the security team for allowlisting via the AV management console.""")
                .build(),

            KnowledgeBaseArticle.builder()
                .title("Insufficient Disk Space During Installation")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern("disk space,not enough space,insufficient space,0x80070070,free space,drive full,storage")
                .category(Category.DISK_SPACE)
                .rootCause("""
                    Installers that report insufficient disk space can fail for three distinct \
                    reasons: the destination drive genuinely lacks the free space required by the \
                    installer's declared SummaryInformation; the system drive (usually C:) lacks \
                    space even if the install target is a different drive, because Windows Installer \
                    always stages temporary files to %TEMP% on the system drive; or the Volume Shadow \
                    Copy Service (VSS) has reserved an unexpectedly large portion of available space. \
                    In some cases, the installer's stated disk requirement is an overestimate, but \
                    the condition still blocks installation until space is freed.""")
                .resolutionSteps("""
                    1. **Check available space** — Open File Explorer, right-click the target drive → Properties. Confirm free space exceeds the installer's stated requirement by at least 20 % to account for temp files.

                    2. **Run Disk Cleanup** — Search for "Disk Cleanup" → select C: → check all categories including "Windows Update Cleanup". Click "Clean up system files" for deeper cleaning.

                    3. **Clear %TEMP%** — Press Win+R, type `%TEMP%`, press Enter. Select all files (Ctrl+A) and delete. Skip any files in use.

                    4. **Move the install target** — Most installers allow you to change the destination directory. Point to a drive with adequate free space.

                    5. **Reduce VSS shadow storage** — From an elevated prompt:
                       ```
                       vssadmin list shadowstorage
                       vssadmin resize shadowstorage /For=C: /On=C: /MaxSize=5GB
                       ```

                    6. **Retry the installation** — After freeing space, relaunch the installer. If the error persists, reboot first to clear residual temp files locked by previous attempts.""")
                .build(),

            KnowledgeBaseArticle.builder()
                .title("Administrator Elevation / UAC Prompt Required")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern("access denied,administrator,uac,elevation required,run as administrator,privilege,0x80070005,permission denied")
                .category(Category.PERMISSION)
                .rootCause("""
                    Windows Installer enforces a privilege separation model: operations that write \
                    to %ProgramFiles%, %ProgramData%, or HKEY_LOCAL_MACHINE require an elevated \
                    (administrator) token. When User Account Control (UAC) is enabled and the \
                    installer is launched from a standard user account — or from an administrator \
                    account that hasn't been elevated — Windows returns Access Denied (0x80070005). \
                    Group Policy in domain environments can further restrict UAC behaviour, blocking \
                    elevation dialogs entirely and requiring an administrator to perform the install \
                    remotely or via a software deployment tool.""")
                .resolutionSteps("""
                    1. **Run as Administrator** — Right-click the installer file → "Run as administrator". Accept the UAC prompt. This is the most common fix.

                    2. **Use an administrator account** — If you're on a standard user account, log in with a local or domain administrator account, then run the installer.

                    3. **Launch from an elevated command prompt** — Press Win+X → "Windows Terminal (Admin)" or "Command Prompt (Admin)", then run the installer from that prompt:
                       ```
                       cd "C:\\Downloads"
                       start /wait setup.exe
                       ```

                    4. **Check folder permissions** — If installing to a custom directory, right-click the target folder → Properties → Security. Ensure your user or "SYSTEM" has Full Control. Correct if missing.

                    5. **Domain environments** — Contact your IT administrator to push the installation via Group Policy Software Installation, SCCM/Intune, or to temporarily grant local admin rights for the duration of the install.""")
                .build(),

            KnowledgeBaseArticle.builder()
                .title("Conflicting Older Version Still Installed")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern("older version,previous version,already installed,conflict,uninstall first,upgrade,downgrade,product already exists")
                .category(Category.CORRUPTION)
                .rootCause("""
                    MSI-based installers use a ProductCode GUID and version number to track \
                    installed products. When a newer installer detects a ProductCode from a \
                    different major version in the registry, it may refuse to proceed because its \
                    upgrade table does not list that version as a valid upgrade path. Similarly, \
                    if a previous uninstall completed partially — leaving behind registry keys, \
                    services, or files — the new installer incorrectly believes the old version \
                    is still active and blocks installation to avoid a corrupted mixed-version \
                    state. Silent enterprise deployments are especially prone to leaving orphaned \
                    MSI registrations.""")
                .resolutionSteps("""
                    1. **Standard uninstall** — Settings → Apps → search for the old version → Uninstall. Reboot after uninstallation completes.

                    2. **Use the vendor's dedicated uninstaller** — Many vendors ship a standalone cleanup tool (e.g., Microsoft's `SetupCleanupTool.exe`, Adobe's `Creative Cloud Cleaner Tool`). Use it if a standard uninstall fails or leaves traces.

                    3. **Run the Microsoft Program Install and Uninstall troubleshooter** — Download from Microsoft Support (`MicrosoftProgram_Install_and_Uninstall.meta.diagcab`). It removes stubborn MSI registrations that block new installs.

                    4. **Manually remove residual registry keys** — Open `regedit`, search under:
                       - `HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall`
                       - `HKLM\\SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall`
                       Delete any stale entry for the old product.

                    5. **Retry the new installer** — After a clean removal and a reboot, run the new version's installer as Administrator.""")
                .build(),

            KnowledgeBaseArticle.builder()
                .title(".NET Framework Version Mismatch")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern(".net,dotnet,framework,clr,4.8,4.7,4.6,net6,net7,net8,runtime not found,mixed mode assembly,target framework")
                .category(Category.DEPENDENCY)
                .rootCause("""
                    .NET has two parallel ecosystems: the legacy .NET Framework (versions 1.0–4.8, \
                    Windows-only, ships with the OS) and modern .NET (5, 6, 7, 8+, cross-platform, \
                    installed separately). Applications compiled against a specific Target Framework \
                    Moniker (TFM) will not run on a lower runtime version. A common failure mode is \
                    an application built for .NET Framework 4.8 running on a machine where only 4.6 \
                    is present, or a .NET 6 application run on a machine with only .NET 5. Mixed-mode \
                    assemblies (native + managed) have additional constraints and will fail with \
                    loader errors if the CLR version expected by the native host differs from the \
                    installed runtime.""")
                .resolutionSteps("""
                    1. **Identify the required version** — Check the application's documentation or installer log. For .exe files, you can inspect the required framework with `dotnet --info` or by opening the app's config file (`app.exe.config`) and reading the `<supportedRuntime>` element.

                    2. **Check what is installed** — Run in PowerShell:
                       ```powershell
                       Get-ChildItem 'HKLM:\\SOFTWARE\\Microsoft\\NET Framework Setup\\NDP' -Recurse |
                         Get-ItemProperty -Name Version -EA 0 | Where-Object { $_.PSChildName -match '^[0-9]' }
                       ```
                       For modern .NET: `dotnet --list-runtimes`

                    3. **Install the required .NET Framework** — For legacy versions, use Windows Update or download directly from the Microsoft .NET Framework download page. .NET Framework 4.8 is the final version and is included in Windows 11.

                    4. **Install modern .NET runtime** — Download the specific runtime version from `https://dotnet.microsoft.com/download`. Install both the x86 and x64 variants if unsure.

                    5. **Reboot and retry** — After installing the runtime, reboot to ensure the environment variables and fusion log cache are refreshed, then re-run the installer or application.""")
                .build(),

            KnowledgeBaseArticle.builder()
                .title("Windows Installer Service Not Running")
                .software("Generic")
                .osTarget("Windows 11")
                .errorPattern("windows installer service,msiserver,service could not be accessed,error 1601,installer service not running,msi service")
                .category(Category.REGISTRY)
                .rootCause("""
                    The Windows Installer service (msiserver) must be running for any MSI-based \
                    installation to proceed. Error 1601 ("The Windows Installer service could not \
                    be accessed") means the service is either stopped, its start type is set to \
                    Disabled, or the service registration in the registry is corrupt. This can \
                    happen after aggressive OS hardening scripts, failed Windows Updates, or \
                    third-party optimisation tools that disable non-essential services. In Safe Mode, \
                    msiserver is intentionally stopped — running an installer in Safe Mode will always \
                    trigger this error unless Safe Mode with Networking is used and the service is \
                    started manually.""")
                .resolutionSteps("""
                    1. **Start the service manually** — Open an elevated command prompt and run:
                       ```
                       net start msiserver
                       ```
                       If it starts, retry the installation immediately.

                    2. **Set the service to Manual start** — Press Win+R → `services.msc`. Find "Windows Installer", double-click it, set Startup type to **Manual**, click Start, then OK.

                    3. **Re-register the Windows Installer DLLs** — From an elevated prompt:
                       ```
                       %windir%\\system32\\msiexec.exe /unregister
                       %windir%\\system32\\msiexec.exe /regserver
                       ```
                       Reboot and retry.

                    4. **Check for registry corruption** — Open `regedit` and verify this key exists:
                       `HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\msiserver`
                       If absent or corrupt, export the key from a healthy machine and import it.

                    5. **Run the System File Checker** — From an elevated prompt:
                       ```
                       sfc /scannow
                       ```
                       Allow it to complete. If corruption is found and repaired, reboot and retry the installation.

                    6. **Escalate to OS repair** — If none of the above resolves the issue, run:
                       ```
                       DISM /Online /Cleanup-Image /RestoreHealth
                       ```
                       followed by `sfc /scannow` again. This repairs the Windows component store.""")
                .build()
        );
    }
}
