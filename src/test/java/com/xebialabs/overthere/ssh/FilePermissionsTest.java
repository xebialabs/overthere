package com.xebialabs.overthere.ssh;

import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FilePermissionsTest {

    @Test
    public void shouldCorrectlyParseRead() {
        assertThat(new FilePermissions("test", "test", "d---------").canRead("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "dr--------").canRead("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drw-------").canRead("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "dr-x------").canRead("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "dr-s------").canRead("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwx------").canRead("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drws------").canRead("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwx------").canRead("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxr-----").canRead("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrw----").canRead("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxr-x---").canRead("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxr-s---").canRead("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwx---").canRead("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrws---").canRead("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwx---").canRead("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxr--").canRead("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxrw-").canRead("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxr-x").canRead("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxr-t").canRead("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxrwx").canRead("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxrwt").canRead("john", newArrayList("john")), equalTo(true));
    }

    @Test
    public void shouldCorrectlyParseWrite() {
        assertThat(new FilePermissions("test", "test", "d---------").canWrite("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "dr--------").canWrite("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drw-------").canWrite("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "dr-x------").canWrite("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "dr-s------").canWrite("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwx------").canWrite("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drws------").canWrite("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwx------").canWrite("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxr-----").canWrite("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrw----").canWrite("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxr-x---").canWrite("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxr-s---").canWrite("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwx---").canWrite("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrws---").canWrite("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwx---").canWrite("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxr--").canWrite("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxrw-").canWrite("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxr-x").canWrite("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxr-t").canWrite("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxrwx").canWrite("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxrwt").canWrite("john", newArrayList("john")), equalTo(true));
    }

    @Test
    public void shouldCorrectlyParseExecute() {
        assertThat(new FilePermissions("test", "test", "d---------").canExecute("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "dr--------").canExecute("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drw-------").canExecute("test", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "dr-x------").canExecute("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "dr-s------").canExecute("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwx------").canExecute("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drws------").canExecute("test", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwx------").canExecute("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxr-----").canExecute("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrw----").canExecute("john", newArrayList("test")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxr-x---").canExecute("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxr-s---").canExecute("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwx---").canExecute("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrws---").canExecute("john", newArrayList("test")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwx---").canExecute("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxr--").canExecute("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxrw-").canExecute("john", newArrayList("john")), equalTo(false));
        assertThat(new FilePermissions("test", "test", "drwxrwxr-x").canExecute("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxr-t").canExecute("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxrwx").canExecute("john", newArrayList("john")), equalTo(true));
        assertThat(new FilePermissions("test", "test", "drwxrwxrwt").canExecute("john", newArrayList("john")), equalTo(true));
    }

}
