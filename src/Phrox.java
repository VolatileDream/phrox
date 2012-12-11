import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@SuppressWarnings("static-access")
public class Phrox {

	public static void main( String[] str ){
		
		CommandLineParser clp = new BasicParser();
		Options opts = getOptions();
		try {
			CommandLine cli = clp.parse(opts, str);
			for( Option o : cli.getOptions() ){
				if( cli.hasOption( o.getOpt() ) ){
					System.out.println( o.getOpt() +":"+ print(cli.getOptionValues( o.getOpt() ))  );
				}
			}
		} catch (ParseException e) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("phrox", opts);
			System.exit(1);
		}
	}

	public static Options getOptions(){
		Options opt = new Options();
		
		opt.addOptionGroup( securityOptions() );
		opt.addOptionGroup( networkOptions() );
		
		Option config =
			OptionBuilder
			.hasArg()
			.withArgName("file")
			.withDescription("use the specified config file")
			.withLongOpt("config")
			.create('c');
		
		opt.addOption( config );
		
		Option update =
			OptionBuilder
			.hasArg(false)
			.withDescription("update the program")
			.withLongOpt("update")
			.create('u');
		
		opt.addOption(update);
		
		return opt;
	}
	
	private static OptionGroup networkOptions(){
		OptionGroup grp = new OptionGroup();

		Option listen = 
			OptionBuilder
			.hasArg(false)
			.withDescription("listens for server location broadcasts")
			.withLongOpt("listen")
			.create('l');
		
		grp.addOption( listen );
		
		Option broadcast =
			OptionBuilder
			.hasArg(false)
			.withDescription("turns on broadcast of server address and port via multicast")
			.withLongOpt("broadcast")
			.create('b');
		grp.addOption(broadcast);
		
		return grp;
	}
	
	private static OptionGroup securityOptions(){
		OptionGroup grp = new OptionGroup();
		
		Option regen =
			OptionBuilder
			.hasArg(false)
			.withDescription("(re)generate local keys")
			.withLongOpt("generate-key")
			.create('g');
		grp.addOption(regen);
		
		Option sign =
			OptionBuilder
			.hasArg()
			.withArgName("key-file")
			.withDescription("signs the specified key")
			.withLongOpt("sign-key")
			.create('s');
		grp.addOption(sign);
		
		Option keystore =
			OptionBuilder
			.hasArg()
			.withArgName("file")
			.withDescription("uses the specified keystore")
			.withLongOpt("keystore")
			.create('k');
		grp.addOption( keystore );
		
		return grp;
	} 
	
	private static String print( String[] str ){
		if( str == null || str.length == 0 ){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for( String s : str ){
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString();
	}
		
}
