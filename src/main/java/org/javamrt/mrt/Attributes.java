// This file is part of java-mrt
// A library to parse MRT files

// This file is released under LGPL 3.0
// http://www.gnu.org/licenses/lgpl-3.0-standalone.html

package org.javamrt.mrt;

import java.net.InetAddress;
import java.util.Vector;
import org.javamrt.utils.Debug;
import org.javamrt.utils.RecordAccess;

public class Attributes {
	private String toStr = null;
	/*
	 * private Vector <Attribute> getAttributes () { return attributes; }
	 */
	private final Vector<Attribute> attributes = new Vector<>(MRTConstants.ATTRIBUTE_TOTAL + 1);

	public Attributes(final byte[] record, final int attrLen, final int attrPos, final int attrBytes, final boolean addPath)
			throws Exception {
		if (attrBytes != 2 && attrBytes != 4) {
			throw new AttributeException(String.format(
					"Attributes needs attrBytes 2 or 4 (not %d", attrBytes));
		}
		decode(record, attrLen, attrPos, attrBytes, addPath);
	}

	public Attributes(final byte[] record, final int attrLen, final int attrPos, final boolean addPath) throws Exception {
		decode(record, attrLen, attrPos, 2, addPath);
	}

	private void decode(final byte[] record, final int attrLen, final int attrPos, final int attrBytes, final boolean addPath)
			throws Exception {
		byte[] buffer;

		int here = attrPos;

		if (Debug.compileDebug) {
			Debug.printf("Attributes(...,%d,%d,%d)\n", attrLen, attrPos,
					attrBytes);
		}

		for (int i = 0; i <= MRTConstants.ATTRIBUTE_TOTAL; i++) {
			if (i == MRTConstants.ATTRIBUTE_NEXT_HOP) {
				attributes.addElement(new NextHop());
			} else {
				attributes.addElement(null);
			}
		}

		while (here < attrLen + attrPos) {

			final int flag = RecordAccess.getU8(record, here);
			final int type = RecordAccess.getU8(record, here + 1);
			int len;
			int dato;

			if ((flag & MRTConstants.BGP_ATTR_FLAG_EXTLEN) == 0) {
				len = RecordAccess.getU8(record, here + 2);
				dato = here + 3;
			} else {
				len = RecordAccess.getU16(record, here + 2);
				dato = here + 4;
			}
			buffer = RecordAccess.getBytes(record, dato, len);
			here = dato + len;

			if (Debug.compileDebug) {
				Debug.printf("Flag = 0x%02x(%s) type = %02d Len=%d\n", flag,
						MRTConstants.attrFlags((byte) flag), type, len);
			}

			switch (type) {
			case MRTConstants.AS_PATH:
				final Attribute asPath = new ASPath(buffer, attrBytes);
				attributes.set(MRTConstants.ATTRIBUTE_AS_PATH, asPath);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_AS_PATH = " + asPath);
				}
				break;

			case MRTConstants.ORIGIN:
				final Attribute attrOrigin = new AttrOrigin(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_ORIGIN, attrOrigin);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_ORIGIN, ");
				}
				break;

			case MRTConstants.NEXT_HOP:
				final Attribute nextHop = new NextHop(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_NEXT_HOP, nextHop);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_NEXT_HOP " + nextHop);
				}
				break;

			case MRTConstants.LOCAL_PREF:
				final Attribute localPref = new LocalPref(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_LOCAL_PREF, localPref);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_LOCAL_PREF, ");
				}
				break;

			case MRTConstants.MULTI_EXIT:
				final Attribute med = new Med(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_MULTI_EXIT, med);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_MULTI_EXIT, ");
				}
				break;

			case MRTConstants.COMMUNITY:
				final Attribute community = new Community(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_COMMUNITY, community);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_COMMUNITY, ");
				}
				break;

			case MRTConstants.ATOMIC_AGGREGATE:
				final Attribute atomAggr = new AtomAggr();
				attributes.set(MRTConstants.ATTRIBUTE_ATOMIC_AGGREGATE,
						atomAggr);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_ATOMIC_AGGREGATE, ");
				}
				break;

			case MRTConstants.AGGREGATOR:
				final Attribute aggregator = new Aggregator(buffer, attrBytes);
				attributes.set(MRTConstants.ATTRIBUTE_AGGREGATOR, aggregator);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_AGGREGATOR, ");
				}
				break;

			case MRTConstants.ORIGINATOR_ID:
				final Attribute originatorId = new OriginatorID(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_ORIGINATOR_ID,
						originatorId);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_ORIGINATOR_ID, ");
				}
				break;

			case MRTConstants.CLUSTER_LIST:
				final Attribute clusterList = new ClusterList(buffer);
				attributes
						.set(MRTConstants.ATTRIBUTE_CLUSTER_LIST, clusterList);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_CLUSTER_LIST, ");
				}
				break;

			case MRTConstants.DPA:
				final Attribute dpa = new Dpa(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_DPA, dpa);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_DPA, ");
				}
				break;

			case MRTConstants.ADVERTISER:
				final Attribute advertiser = new Advertiser(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_ADVERTISER, advertiser);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_ADVERTISER, ");
				}
				break;

			case MRTConstants.CLUSTER_ID:
				final Attribute clusterId = new ClusterId(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_CLUSTER_ID, clusterId);
				if (Debug.compileDebug) {
					Debug.printf("ATTRIBUTE_CLUSTER_ID = %s\n", clusterId
							.toString());
				}
				break;

			case MRTConstants.MP_REACH:
				final MpReach mpReach = new MpReach(buffer, addPath);
				attributes.set(MRTConstants.ATTRIBUTE_MP_REACH, mpReach);
				final InetAddress nhia = mpReach.getNextHops().firstElement();
				try {
					final NextHop nh = new NextHop(nhia);
					attributes.set(MRTConstants.ATTRIBUTE_NEXT_HOP, nh);
					if (Debug.compileDebug) {
						Debug.printf("ATTRIBUTE_MP_REACH :%s\n NEXT HOP:%s",
								mpReach.toString(), nh.toString());
					}
				} catch (final NullPointerException npe) {
					// ignore silently
				}
				break;

			case MRTConstants.MP_UNREACH:
				final Attribute mpUnreach = new MpUnReach(buffer, addPath);
				attributes.set(MRTConstants.ATTRIBUTE_MP_UNREACH, mpUnreach);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_MP_UNREACH " + mpUnreach);
				}
				break;

			case MRTConstants.EXT_COMMUNITIES:
				final Attribute extCommunities = new ExtCommunities(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_EXT_COMMUNITIES,
						extCommunities);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_EXT_COMMUNITIES, ");
				}
				break;

			/*
			 * TODO: Handle 4 byte AS stuff correctly
			 *
			 * Observed in rrc01: around April 2007 sometimes AS4PATH is set to
			 * 0
			 */
			case MRTConstants.AS4_PATH:
				if (buffer.length == 0) {
					hasAS4PathBug = true;
				} else {
					/**
					 * throws RFC4893Exception, AttributeException
					 */
				    //					As4Path.replaceAS23456(buffer, (ASPath) getAttribute(MRTConstants.ATTRIBUTE_AS_PATH));
					RFC4893.replaceAS23456(buffer, (ASPath) getAttribute(MRTConstants.ATTRIBUTE_AS_PATH));
				}
				break;

			case MRTConstants.AS4_AGGREGATOR:
				/*
				 * Override 2byte AS Aggregator
				 *
				 * TODO: sanity check: make sure 2 byte aggregator was 23456
				 */
				final Attribute as4Aggregator = new Aggregator(buffer, 4);
				// attributes.set (ATTRIBUTE_AS4_AGGREGATOR, as4Aggregator);
				attributes
						.set(MRTConstants.ATTRIBUTE_AGGREGATOR, as4Aggregator);
				if (Debug.compileDebug) {
					Debug.println("ATTRIBUTE_AS4_AGGREGATOR ");
				}
				break;

			// Expired but present in RRC!

			case MRTConstants.ATTRIBUTE_ASPATHLIMIT:
				final Attribute asPathLimit = new ASPathLimit(buffer);
				attributes.set(MRTConstants.ATTRIBUTE_ASPATHLIMIT, asPathLimit);
				if (Debug.compileDebug) {
					Debug.printf("ATTRIBUTE_ASPATHLIMIT %s\n", asPathLimit
							.toString());
				}
				this.hasASPATHLimit = true;
				break;

			default:
				// throw new AttributeException(type);
			}
		}
	}

	public Attribute getAttribute(final int index) throws Exception {
		return attributes.elementAt(index);
	}

	@Override
	public String toString() {
		if (toStr != null) {
			return toStr;
		}

		toStr = new String();

		for (int i = MRTConstants.ATTRIBUTE_AS_PATH; i <= MRTConstants.ATTRIBUTE_TOTAL; i++) {
			if (attributes.elementAt(i) != null) {
				toStr = toStr.concat(attributes.elementAt(i).toString());
			} else if (i == MRTConstants.ATTRIBUTE_LOCAL_PREF
					|| i == MRTConstants.ATTRIBUTE_MULTI_EXIT) {
				toStr = toStr.concat("0");
			} else if (i == MRTConstants.ATTRIBUTE_ATOMIC_AGGREGATE) {
				toStr = toStr.concat("NAG");
			}
			toStr = toStr.concat("|");
		}
		return toStr;
	}

	public ASPath getASPath() {
		return (ASPath) attributes.elementAt(MRTConstants.ATTRIBUTE_AS_PATH);
	}

	public Community getCommunity() {
		final Community result = (Community) attributes
				.elementAt(MRTConstants.ATTRIBUTE_COMMUNITY);
		if (result != null) {
			return result;
		}
		return Community.empty();
	}

	public Med getMed() {
		final Med result = (Med) attributes
				.elementAt(MRTConstants.ATTRIBUTE_MULTI_EXIT);
		if (result == null) {
			return new Med(0);
		}
		return result;
	}

	public boolean hasAS4PathBug = false;

	public boolean hasASPATHLimit = false;

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o instanceof Attributes) {
			//
			// duplicated if _all_ attributes are the same
			//
			for (int i = 0; i < MRTConstants.ATTRIBUTE_TOTAL; i++) {
				final Attribute a1 = this.attributes.elementAt(i), a2 = ((Attributes) o).attributes
						.elementAt(i);
				if (a1 == null) {
					if (a2 != null) {
						return false;
					}
					continue;
				}
				if (!a1.equals(a2)) {
					return false;
				}
			}
			return true;
		} // else
		return false;
	}
}
